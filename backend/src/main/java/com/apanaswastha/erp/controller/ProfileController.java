package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.enums.BankAccountType;
import com.apanaswastha.erp.enums.OtpType;
import com.apanaswastha.erp.enums.PhotoIdType;
import com.apanaswastha.erp.enums.TwoFaMethod;
import com.apanaswastha.erp.security.JwtTokenProvider;
import com.apanaswastha.erp.service.CurrentUserService;
import com.apanaswastha.erp.service.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/profile", "/api/v1/profile"})
public class ProfileController {

    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;
    private final JwtTokenProvider jwtTokenProvider;

    public ProfileController(CurrentUserService currentUserService,
                             UserProfileService userProfileService,
                             JwtTokenProvider jwtTokenProvider) {
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getProfile() {
        User currentUser = currentUserService.requireCurrentUser();
        return ApiResponse.success("Profile fetched", userProfileService.getOwnProfile(currentUser));
    }

    @PatchMapping("/personal")
    public ApiResponse<Map<String, Object>> updatePersonal(@RequestBody Map<String, Object> payload) {
        User currentUser = currentUserService.requireCurrentUser();
        return ApiResponse.success("Personal information updated", userProfileService.updateOwnPersonal(currentUser, payload));
    }

    @PostMapping("/verify/phone/send")
    public ApiResponse<Map<String, Object>> sendPhoneOtp() {
        return ApiResponse.success("Phone OTP created", userProfileService.sendOtp(currentUserService.requireCurrentUser(), OtpType.PHONE));
    }

    @PostMapping("/verify/phone/confirm")
    public ApiResponse<Map<String, Object>> confirmPhoneOtp(@RequestBody OtpConfirmRequest request) {
        return ApiResponse.success("Phone verified", userProfileService.confirmOtp(currentUserService.requireCurrentUser(), OtpType.PHONE, request.code()));
    }

    @PostMapping("/verify/email/send")
    public ApiResponse<Map<String, Object>> sendEmailOtp() {
        return ApiResponse.success("Email OTP created", userProfileService.sendOtp(currentUserService.requireCurrentUser(), OtpType.EMAIL));
    }

    @PostMapping("/verify/email/confirm")
    public ApiResponse<Map<String, Object>> confirmEmailOtp(@RequestBody OtpConfirmRequest request) {
        return ApiResponse.success("Email verified", userProfileService.confirmOtp(currentUserService.requireCurrentUser(), OtpType.EMAIL, request.code()));
    }

    @PostMapping("/verify/aadhaar")
    public ApiResponse<Map<String, Object>> submitAadhaar(@RequestBody AadhaarRequest request) {
        return ApiResponse.success("Aadhaar submitted", userProfileService.submitAadhaar(currentUserService.requireCurrentUser(), request.aadhaarLast4(), request.aadhaarDocUrl()));
    }

    @PostMapping("/verify/photo-id")
    public ApiResponse<Map<String, Object>> submitPhotoId(@RequestBody PhotoIdRequest request) {
        return ApiResponse.success("Photo ID submitted", userProfileService.submitPhotoId(currentUserService.requireCurrentUser(), request.photoIdType(), request.photoIdDocUrl()));
    }

    @GetMapping("/bank-accounts")
    public ApiResponse<List<Map<String, Object>>> listBankAccounts() {
        return ApiResponse.success("Bank accounts fetched", userProfileService.listBankAccounts(currentUserService.requireCurrentUser()));
    }

    @PostMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addBankAccount(@RequestBody AddBankAccountRequest request) {
        Map<String, Object> response = userProfileService.addBankAccount(
                currentUserService.requireCurrentUser(),
                request.holderName(),
                request.bankName(),
                request.accountNumber(),
                request.ifscCode(),
                request.accountType(),
                request.isPrimary()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account added", response));
    }

    @PatchMapping("/bank-accounts/{id}/primary")
    public ApiResponse<Map<String, Object>> setPrimary(@PathVariable Long id, @RequestBody PasswordConfirmRequest request) {
        return ApiResponse.success("Primary account updated", userProfileService.setPrimaryBankAccount(currentUserService.requireCurrentUser(), id, request.password()));
    }

    @DeleteMapping("/bank-accounts/{id}")
    public ApiResponse<Void> removeBankAccount(@PathVariable Long id) {
        userProfileService.deleteBankAccount(currentUserService.requireCurrentUser(), id);
        return ApiResponse.success("Bank account removed", null);
    }

    @PatchMapping("/security/password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(currentUserService.requireCurrentUser(), request.currentPassword(), request.newPassword());
        return ApiResponse.success("Password changed", null);
    }

    @GetMapping("/security/sessions")
    public ApiResponse<List<Map<String, Object>>> listSessions() {
        return ApiResponse.success("Active sessions fetched", userProfileService.listSessions(currentUserService.requireCurrentUser()));
    }

    @DeleteMapping("/security/sessions/{id}")
    public ApiResponse<Void> revokeSession(@PathVariable Long id) {
        userProfileService.revokeSession(currentUserService.requireCurrentUser(), id);
        return ApiResponse.success("Session revoked", null);
    }

    @DeleteMapping("/security/sessions")
    public ApiResponse<Void> revokeOtherSessions(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = currentUserService.requireCurrentUser();
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        String jti = token == null ? null : jwtTokenProvider.extractJti(token);
        Long currentSessionId = jti == null ? null : userProfileService.sessionIdByJti(user, jti);
        userProfileService.revokeOtherSessions(user, currentSessionId);
        return ApiResponse.success("Other sessions revoked", null);
    }

    @GetMapping("/security/login-history")
    public ApiResponse<List<Map<String, Object>>> loginHistory() {
        return ApiResponse.success("Login history fetched", userProfileService.listLoginHistory(currentUserService.requireCurrentUser()));
    }

    @PatchMapping("/security/2fa")
    public ApiResponse<Map<String, Object>> updateTwoFa(@RequestBody TwoFaRequest request) {
        return ApiResponse.success("2FA updated", userProfileService.updateTwoFa(currentUserService.requireCurrentUser(), request.enabled(), request.method()));
    }

    @GetMapping("/preferences")
    public ApiResponse<Map<String, Object>> getPreferences() {
        return ApiResponse.success("Preferences fetched", userProfileService.getPreferences(currentUserService.requireCurrentUser()));
    }

    @PatchMapping("/preferences")
    public ApiResponse<Map<String, Object>> updatePreferences(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success("Preferences updated", userProfileService.updatePreferences(currentUserService.requireCurrentUser(), payload));
    }

    @PostMapping("/deactivate")
    public ApiResponse<Void> deactivate(@RequestBody PasswordConfirmRequest request) {
        userProfileService.deactivateSelf(currentUserService.requireCurrentUser(), request.password());
        return ApiResponse.success("Account deactivated", null);
    }

    @PostMapping("/data-deletion-request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dataDeletionRequest(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = userProfileService.requestDataDeletion(
                currentUserService.requireCurrentUser(),
                payload.get("reason") == null ? null : String.valueOf(payload.get("reason"))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Deletion request submitted", result));
    }

    public record OtpConfirmRequest(@NotBlank String code) {
    }

    public record AadhaarRequest(@NotBlank String aadhaarLast4, String aadhaarDocUrl) {
    }

    public record PhotoIdRequest(PhotoIdType photoIdType, String photoIdDocUrl) {
    }

    public record AddBankAccountRequest(String holderName, String bankName, String accountNumber, String ifscCode,
                                        BankAccountType accountType, boolean isPrimary) {
    }

    public record PasswordConfirmRequest(@NotBlank String password) {
    }

    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {
    }

    public record TwoFaRequest(boolean enabled, TwoFaMethod method) {
    }
}
