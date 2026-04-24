package com.apanaswastha.erp.service;

import com.apanaswastha.erp.entity.*;
import com.apanaswastha.erp.enums.*;
import com.apanaswastha.erp.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserProfileService {

    private static final String IFSC_REGEX = "^[A-Z]{4}0[A-Z0-9]{6}$";

    private final UserProfileRepository userProfileRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final UserBankAccountRepository userBankAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final User2FaSettingRepository user2FaSettingRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final DataDeletionRequestRepository dataDeletionRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankCryptoService bankCryptoService;
    private final AuthSessionService authSessionService;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              UserVerificationRepository userVerificationRepository,
                              OtpCodeRepository otpCodeRepository,
                              UserBankAccountRepository userBankAccountRepository,
                              UserSessionRepository userSessionRepository,
                              LoginHistoryRepository loginHistoryRepository,
                              User2FaSettingRepository user2FaSettingRepository,
                              UserPreferenceRepository userPreferenceRepository,
                              DataDeletionRequestRepository dataDeletionRequestRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              BankCryptoService bankCryptoService,
                              AuthSessionService authSessionService) {
        this.userProfileRepository = userProfileRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.userBankAccountRepository = userBankAccountRepository;
        this.userSessionRepository = userSessionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.user2FaSettingRepository = user2FaSettingRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.dataDeletionRequestRepository = dataDeletionRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bankCryptoService = bankCryptoService;
        this.authSessionService = authSessionService;
    }

    @Transactional
    public void initializeProfileTables(User user) {
        ensureProfile(user);
        ensureVerification(user);
        ensure2Fa(user);
        ensurePreferences(user);
    }

    @Transactional
    public Map<String, Object> getOwnProfile(User user) {
        UserProfile profile = ensureProfile(user);
        UserVerification verification = ensureVerification(user);
        User2FaSetting twoFa = ensure2Fa(user);
        UserPreference preferences = ensurePreferences(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("role", user.getRole().getName().name());
        response.put("status", user.getStatus().name());
        response.put("sectionVisibility", buildSectionVisibility(user.getRole().getName()));
        response.put("personalInformation", toPersonalMap(profile));
        response.put("verification", toVerificationMap(verification));
        response.put("bankAccounts", listBankAccounts(user));
        response.put("securitySettings", Map.of(
                "sessions", listSessions(user),
                "loginHistory", listLoginHistory(user),
                "twoFactor", Map.of("enabled", twoFa.isEnabled(), "method", twoFa.getMethod().name().toLowerCase())
        ));
        response.put("preferences", toPreferencesMap(preferences));
        return response;
    }

    @Transactional
    public Map<String, Object> updateOwnPersonal(User user, Map<String, Object> payload) {
        rejectDangerousFields(payload);
        return updatePersonal(user, user, payload);
    }

    @Transactional
    public Map<String, Object> updatePersonalByAdmin(User actor, Long targetUserId, Map<String, Object> payload) {
        if (!(actor.getRole().getName() == RoleName.SUPER_ADMIN || actor.getRole().getName() == RoleName.ADMIN)) {
            throw new AccessDeniedException("Only Admin and Super Admin can edit another user's personal info");
        }
        rejectDangerousFields(payload);
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        return updatePersonal(actor, target, payload);
    }

    @Transactional
    public Map<String, Object> sendOtp(User user, OtpType otpType) {
        UserVerification verification = ensureVerification(user);
        Instant now = Instant.now();

        if (otpType == OtpType.PHONE && verification.getPhoneLockedUntil() != null && verification.getPhoneLockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("Phone verification temporarily locked");
        }
        if (otpType == OtpType.EMAIL && verification.getEmailLockedUntil() != null && verification.getEmailLockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("Email verification temporarily locked");
        }

        otpCodeRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), otpType).ifPresent(last -> {
            if (last.getCreatedAt().isAfter(now.minusSeconds(60))) {
                throw new TooManyOtpRequestsException("OTP request rate limited. Try again after 60 seconds");
            }
        });

        OtpCode otpCode = new OtpCode();
        otpCode.setUser(user);
        otpCode.setType(otpType);
        otpCode.setCode(generateOtp());
        otpCode.setExpiresAt(now.plus(otpType == OtpType.PHONE ? 10 : 30, ChronoUnit.MINUTES));
        otpCodeRepository.save(otpCode);

        return Map.of("type", otpType.name().toLowerCase(), "expiresAt", otpCode.getExpiresAt());
    }

    @Transactional
    public Map<String, Object> confirmOtp(User user, OtpType otpType, String code) {
        UserVerification verification = ensureVerification(user);
        Instant now = Instant.now();

        if (otpType == OtpType.PHONE && verification.getPhoneLockedUntil() != null && verification.getPhoneLockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("Phone verification temporarily locked");
        }
        if (otpType == OtpType.EMAIL && verification.getEmailLockedUntil() != null && verification.getEmailLockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("Email verification temporarily locked");
        }

        OtpCode otpCode = otpCodeRepository.findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(user.getId(), otpType)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found"));

        if (otpCode.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (!otpCode.getCode().equals(code)) {
            if (otpType == OtpType.PHONE) {
                verification.setPhoneFailedAttempts(verification.getPhoneFailedAttempts() + 1);
                if (verification.getPhoneFailedAttempts() >= 5) {
                    verification.setPhoneLockedUntil(now.plus(15, ChronoUnit.MINUTES));
                }
            } else {
                verification.setEmailFailedAttempts(verification.getEmailFailedAttempts() + 1);
                if (verification.getEmailFailedAttempts() >= 5) {
                    verification.setEmailLockedUntil(now.plus(15, ChronoUnit.MINUTES));
                }
            }
            userVerificationRepository.save(verification);
            throw new IllegalArgumentException("Invalid OTP");
        }

        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);

        if (otpType == OtpType.PHONE) {
            verification.setPhoneVerified(true);
            verification.setPhoneVerifiedAt(now);
            verification.setPhoneFailedAttempts(0);
        } else {
            verification.setEmailVerified(true);
            verification.setEmailVerifiedAt(now);
            verification.setEmailFailedAttempts(0);
        }
        userVerificationRepository.save(verification);

        return Map.of("verified", true, "type", otpType.name().toLowerCase());
    }

    @Transactional
    public Map<String, Object> submitAadhaar(User user, String aadhaarLast4, String aadhaarDocUrl) {
        if (aadhaarLast4 == null || !aadhaarLast4.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Only Aadhaar last 4 digits are allowed");
        }
        UserVerification verification = ensureVerification(user);
        verification.setAadhaarLast4(aadhaarLast4);
        verification.setAadhaarDocUrl(aadhaarDocUrl);
        verification.setAadhaarStatus(VerificationStatus.PENDING_REVIEW);
        userVerificationRepository.save(verification);
        return Map.of("aadhaarStatus", verification.getAadhaarStatus().name().toLowerCase());
    }

    @Transactional
    public Map<String, Object> submitPhotoId(User user, PhotoIdType photoIdType, String photoIdDocUrl) {
        UserVerification verification = ensureVerification(user);
        verification.setPhotoIdType(photoIdType);
        verification.setPhotoIdDocUrl(photoIdDocUrl);
        verification.setPhotoIdStatus(VerificationStatus.PENDING_REVIEW);
        verification.setPhotoIdRejectReason(null);
        userVerificationRepository.save(verification);
        return Map.of("photoIdStatus", verification.getPhotoIdStatus().name().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listBankAccounts(User user) {
        return userBankAccountRepository.findByUserIdAndDeletedFalse(user.getId()).stream()
                .map(this::toBankAccountMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> addBankAccount(User user, String holderName, String bankName, String accountNumber,
                                              String ifscCode, BankAccountType accountType, boolean isPrimary) {
        if (ifscCode == null || !ifscCode.matches(IFSC_REGEX)) {
            throw new IllegalArgumentException("Invalid IFSC code format");
        }
        if (accountNumber == null || accountNumber.length() < 8) {
            throw new IllegalArgumentException("Invalid account number");
        }

        UserBankAccount account = new UserBankAccount();
        account.setUser(user);
        account.setHolderName(holderName);
        account.setBankName(bankName);
        account.setAccountNumber(bankCryptoService.encrypt(accountNumber));
        account.setIfscCode(ifscCode);
        account.setAccountType(accountType == null ? BankAccountType.SAVINGS : accountType);

        if (isPrimary) {
            userBankAccountRepository.findByUserIdAndPrimaryTrueAndDeletedFalse(user.getId())
                    .ifPresent(existing -> {
                        existing.setPrimary(false);
                        userBankAccountRepository.save(existing);
                    });
            account.setPrimary(true);
        }

        userBankAccountRepository.save(account);
        return toBankAccountMap(account);
    }

    @Transactional
    public Map<String, Object> setPrimaryBankAccount(User user, Long bankAccountId, String password) {
        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password confirmation required");
        }

        UserBankAccount target = userBankAccountRepository.findByIdAndUserIdAndDeletedFalse(bankAccountId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));

        userBankAccountRepository.findByUserIdAndPrimaryTrueAndDeletedFalse(user.getId())
                .ifPresent(existing -> {
                    existing.setPrimary(false);
                    userBankAccountRepository.save(existing);
                });

        target.setPrimary(true);
        userBankAccountRepository.save(target);
        return toBankAccountMap(target);
    }

    @Transactional
    public void deleteBankAccount(User user, Long bankAccountId) {
        UserBankAccount target = userBankAccountRepository.findByIdAndUserIdAndDeletedFalse(bankAccountId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
        target.setDeleted(true);
        target.setPrimary(false);
        userBankAccountRepository.save(target);
    }

    @Transactional
    public void verifyBankAccount(User actor, Long userId, Long bankAccountId) {
        if (!(actor.getRole().getName() == RoleName.SUPER_ADMIN || actor.getRole().getName() == RoleName.ADMIN)) {
            throw new AccessDeniedException("Only admin roles can verify bank accounts");
        }
        UserBankAccount account = userBankAccountRepository.findByIdAndUserIdAndDeletedFalse(bankAccountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
        account.setVerified(true);
        account.setVerifiedBy(actor);
        account.setVerifiedAt(Instant.now());
        userBankAccountRepository.save(account);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || !newPassword.matches("^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$")) {
            throw new IllegalArgumentException("Password must have at least 8 chars, one number, one special character");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        authSessionService.revokeAllSessions(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listSessions(User user) {
        return userSessionRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(user.getId(), Instant.now()).stream()
                .map(session -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", session.getId());
                    map.put("deviceInfo", session.getDeviceInfo());
                    map.put("ipAddress", session.getIpAddress());
                    map.put("location", session.getLocation());
                    map.put("createdAt", session.getCreatedAt());
                    map.put("lastActive", session.getLastActive());
                    map.put("expiresAt", session.getExpiresAt());
                    return map;
                })
                .toList();
    }

    @Transactional
    public void revokeSession(User user, Long sessionId) {
        UserSession session = userSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.setRevoked(true);
        userSessionRepository.save(session);
    }

    @Transactional
    public void revokeOtherSessions(User user, Long currentSessionId) {
        authSessionService.revokeOtherSessions(user.getId(), currentSessionId);
    }

    @Transactional(readOnly = true)
    public Long sessionIdByJti(User user, String jti) {
        return userSessionRepository.findByJtiAndUserId(jti, user.getId())
                .map(UserSession::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listLoginHistory(User user) {
        return loginHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(history -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("createdAt", history.getCreatedAt());
                    map.put("deviceInfo", history.getDeviceInfo());
                    map.put("ipAddress", history.getIpAddress());
                    map.put("location", history.getLocation());
                    map.put("success", history.isSuccess());
                    return map;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> updateTwoFa(User user, boolean enabled, TwoFaMethod method) {
        UserVerification verification = ensureVerification(user);
        if (enabled && method == TwoFaMethod.SMS && !verification.isPhoneVerified()) {
            throw new IllegalArgumentException("Phone must be verified for SMS 2FA");
        }
        if (enabled && method == TwoFaMethod.EMAIL && !verification.isEmailVerified()) {
            throw new IllegalArgumentException("Email must be verified for Email 2FA");
        }

        User2FaSetting setting = ensure2Fa(user);
        setting.setEnabled(enabled);
        setting.setMethod(method == null ? TwoFaMethod.SMS : method);
        user2FaSettingRepository.save(setting);

        return Map.of("enabled", setting.isEnabled(), "method", setting.getMethod().name().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPreferences(User user) {
        return toPreferencesMap(ensurePreferences(user));
    }

    @Transactional
    public Map<String, Object> updatePreferences(User user, Map<String, Object> payload) {
        UserPreference preference = ensurePreferences(user);
        if (payload.containsKey("language")) {
            preference.setLanguage(String.valueOf(payload.get("language")));
        }
        if (payload.containsKey("theme")) {
            preference.setTheme(String.valueOf(payload.get("theme")));
        }
        if (payload.containsKey("notifInapp")) {
            preference.setNotifInapp(String.valueOf(payload.get("notifInapp")));
        }
        if (payload.containsKey("notifSms")) {
            preference.setNotifSms(String.valueOf(payload.get("notifSms")));
        }
        if (payload.containsKey("notifEmail")) {
            preference.setNotifEmail(String.valueOf(payload.get("notifEmail")));
        }
        userPreferenceRepository.save(preference);
        return toPreferencesMap(preference);
    }

    @Transactional
    public void deactivateSelf(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
        authSessionService.revokeAllSessions(user.getId());
    }

    @Transactional
    public Map<String, Object> requestDataDeletion(User user, String reason) {
        if (user.getRole().getName() != RoleName.FAMILY) {
            throw new AccessDeniedException("Only Family accounts can request data deletion");
        }
        DataDeletionRequest request = new DataDeletionRequest();
        request.setUser(user);
        request.setReason(reason);
        dataDeletionRequestRepository.save(request);
        return Map.of("id", request.getId(), "status", request.getStatus());
    }

    @Transactional
    public Map<String, Object> reviewAadhaar(User actor, Long userId, boolean approve, String rejectReason) {
        if (!(actor.getRole().getName() == RoleName.SUPER_ADMIN || actor.getRole().getName() == RoleName.ADMIN)) {
            throw new AccessDeniedException("Only admin roles can review Aadhaar");
        }
        UserVerification verification = userVerificationRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Verification record not found"));
        verification.setAadhaarReviewedBy(actor);
        if (approve) {
            verification.setAadhaarStatus(VerificationStatus.VERIFIED);
            verification.setAadhaarVerifiedAt(Instant.now());
        } else {
            verification.setAadhaarStatus(VerificationStatus.REJECTED);
        }
        userVerificationRepository.save(verification);
        return Map.of("aadhaarStatus", verification.getAadhaarStatus().name().toLowerCase(), "reason", rejectReason);
    }

    @Transactional
    public Map<String, Object> reviewPhotoId(User actor, Long userId, boolean approve, String rejectReason) {
        if (!(actor.getRole().getName() == RoleName.SUPER_ADMIN || actor.getRole().getName() == RoleName.ADMIN)) {
            throw new AccessDeniedException("Only admin roles can review photo ID");
        }
        UserVerification verification = userVerificationRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Verification record not found"));
        verification.setPhotoIdReviewedBy(actor);
        if (approve) {
            verification.setPhotoIdStatus(VerificationStatus.VERIFIED);
            verification.setPhotoIdVerifiedAt(Instant.now());
            verification.setPhotoIdRejectReason(null);
        } else {
            verification.setPhotoIdStatus(VerificationStatus.REJECTED);
            verification.setPhotoIdRejectReason(rejectReason);
        }
        userVerificationRepository.save(verification);
        return Map.of("photoIdStatus", verification.getPhotoIdStatus().name().toLowerCase(), "reason", verification.getPhotoIdRejectReason());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return getOwnProfile(user);
    }

    private Map<String, Object> updatePersonal(User actor, User target, Map<String, Object> payload) {
        UserProfile profile = ensureProfile(target);
        UserVerification verification = ensureVerification(target);

        String incomingPhone = getNullableString(payload, "phone");
        String incomingEmail = getNullableString(payload, "email");

        if (incomingPhone != null && !incomingPhone.equals(profile.getPhone())) {
            profile.setPhone(incomingPhone);
            target.setPhone(incomingPhone);
            verification.setPhoneVerified(false);
            verification.setPhoneVerifiedAt(null);
            verification.setPhoneFailedAttempts(0);
        }
        if (incomingEmail != null && !incomingEmail.equals(profile.getEmail())) {
            profile.setEmail(incomingEmail);
            target.setEmail(incomingEmail);
            verification.setEmailVerified(false);
            verification.setEmailVerifiedAt(null);
            verification.setEmailFailedAttempts(0);
        }

        if (payload.containsKey("fullName")) profile.setFullName(getNullableString(payload, "fullName"));
        if (payload.containsKey("photoUrl")) profile.setPhotoUrl(getNullableString(payload, "photoUrl"));
        if (payload.containsKey("gender")) profile.setGender(getNullableString(payload, "gender"));
        if (payload.containsKey("addressStreet")) profile.setAddressStreet(getNullableString(payload, "addressStreet"));
        if (payload.containsKey("addressCity")) profile.setAddressCity(getNullableString(payload, "addressCity"));
        if (payload.containsKey("addressDistrict")) profile.setAddressDistrict(getNullableString(payload, "addressDistrict"));
        if (payload.containsKey("addressState")) profile.setAddressState(getNullableString(payload, "addressState"));
        if (payload.containsKey("addressPincode")) profile.setAddressPincode(getNullableString(payload, "addressPincode"));
        if (payload.containsKey("emergencyName")) profile.setEmergencyName(getNullableString(payload, "emergencyName"));
        if (payload.containsKey("emergencyPhone")) profile.setEmergencyPhone(getNullableString(payload, "emergencyPhone"));

        userProfileRepository.save(profile);
        userVerificationRepository.save(verification);
        userRepository.save(target);

        Map<String, Object> response = toPersonalMap(profile);
        response.put("updatedBy", actor.getId());
        return response;
    }

    private void rejectDangerousFields(Map<String, Object> payload) {
        List<String> disallowed = List.of(
                "role",
                "roleId",
                "assignedStateId",
                "assignedDistrictId",
                "assignedBlockId",
                "assignedCenterId",
                "commission",
                "commissionRate",
                "scope"
        );
        for (String key : payload.keySet()) {
            if (disallowed.contains(key)) {
                throw new AccessDeniedException("Dangerous fields are not allowed in personal updates");
            }
        }
    }

    private String getNullableString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private UserProfile ensureProfile(User user) {
        return userProfileRepository.findById(user.getId()).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setFullName(user.getUsername());
            profile.setPhone(user.getPhone() == null ? "" : user.getPhone());
            profile.setEmail(user.getEmail());
            return userProfileRepository.save(profile);
        });
    }

    private UserVerification ensureVerification(User user) {
        return userVerificationRepository.findById(user.getId()).orElseGet(() -> {
            UserVerification verification = new UserVerification();
            verification.setUser(user);
            return userVerificationRepository.save(verification);
        });
    }

    private User2FaSetting ensure2Fa(User user) {
        return user2FaSettingRepository.findById(user.getId()).orElseGet(() -> {
            User2FaSetting setting = new User2FaSetting();
            setting.setUser(user);
            return user2FaSettingRepository.save(setting);
        });
    }

    private UserPreference ensurePreferences(User user) {
        return userPreferenceRepository.findById(user.getId()).orElseGet(() -> {
            UserPreference preference = new UserPreference();
            preference.setUser(user);
            return userPreferenceRepository.save(preference);
        });
    }

    private Map<String, Boolean> buildSectionVisibility(RoleName roleName) {
        boolean isFamily = roleName == RoleName.FAMILY;
        boolean bankVisible = !isFamily;
        boolean permissionSummary = roleName != RoleName.FAMILY;

        Map<String, Boolean> sections = new LinkedHashMap<>();
        sections.put("personalInformation", true);
        sections.put("verification", true);
        sections.put("bankAccount", bankVisible);
        sections.put("securitySettings", true);
        sections.put("permissionSummary", permissionSummary);
        sections.put("preferences", true);
        sections.put("dangerZone", true);
        return sections;
    }

    private Map<String, Object> toPersonalMap(UserProfile profile) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fullName", profile.getFullName());
        map.put("photoUrl", profile.getPhotoUrl());
        map.put("dateOfBirth", profile.getDateOfBirth());
        map.put("gender", profile.getGender());
        map.put("phone", profile.getPhone());
        map.put("email", profile.getEmail());
        map.put("addressStreet", profile.getAddressStreet());
        map.put("addressCity", profile.getAddressCity());
        map.put("addressDistrict", profile.getAddressDistrict());
        map.put("addressState", profile.getAddressState());
        map.put("addressPincode", profile.getAddressPincode());
        map.put("emergencyName", profile.getEmergencyName());
        map.put("emergencyPhone", profile.getEmergencyPhone());
        return map;
    }

    private Map<String, Object> toVerificationMap(UserVerification verification) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("phoneVerified", verification.isPhoneVerified());
        map.put("emailVerified", verification.isEmailVerified());
        map.put("aadhaarStatus", verification.getAadhaarStatus().name().toLowerCase());
        map.put("photoIdStatus", verification.getPhotoIdStatus().name().toLowerCase());
        map.put("photoIdRejectReason", verification.getPhotoIdRejectReason());
        return map;
    }

    private Map<String, Object> toPreferencesMap(UserPreference preference) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("language", preference.getLanguage());
        map.put("theme", preference.getTheme());
        map.put("notifInapp", preference.getNotifInapp());
        map.put("notifSms", preference.getNotifSms());
        map.put("notifEmail", preference.getNotifEmail());
        return map;
    }

    private Map<String, Object> toBankAccountMap(UserBankAccount account) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", account.getId());
        map.put("holderName", account.getHolderName());
        map.put("bankName", account.getBankName());
        map.put("accountNumber", maskAccount(bankCryptoService.decrypt(account.getAccountNumber())));
        map.put("ifscCode", account.getIfscCode());
        map.put("accountType", account.getAccountType().name().toLowerCase());
        map.put("isPrimary", account.isPrimary());
        map.put("isVerified", account.isVerified());
        return map;
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "••••••" + accountNumber.substring(accountNumber.length() - 4);
    }

    private String generateOtp() {
        int otp = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.valueOf(otp);
    }

    public static class TooManyOtpRequestsException extends RuntimeException {
        public TooManyOtpRequestsException(String message) {
            super(message);
        }
    }
}
