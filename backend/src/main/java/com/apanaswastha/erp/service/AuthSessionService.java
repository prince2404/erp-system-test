package com.apanaswastha.erp.service;

import com.apanaswastha.erp.entity.LoginHistory;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.UserSession;
import com.apanaswastha.erp.repository.LoginHistoryRepository;
import com.apanaswastha.erp.repository.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class AuthSessionService {

    private final UserSessionRepository userSessionRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public AuthSessionService(UserSessionRepository userSessionRepository,
                              LoginHistoryRepository loginHistoryRepository) {
        this.userSessionRepository = userSessionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @Transactional
    public void createSession(User user, String jti, String token, Instant expiresAt, String deviceInfo, String ipAddress) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setJti(jti);
        session.setTokenHash(sha256(token));
        session.setExpiresAt(expiresAt);
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setLocation("Unknown");
        userSessionRepository.save(session);
    }

    @Transactional
    public void recordLogin(User user, boolean success, String deviceInfo, String ipAddress) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setSuccess(success);
        loginHistory.setDeviceInfo(deviceInfo);
        loginHistory.setIpAddress(ipAddress);
        loginHistory.setLocation("Approximate location unavailable");
        loginHistoryRepository.save(loginHistory);
    }

    public boolean isSessionActive(String jti) {
        Optional<UserSession> session = userSessionRepository.findByJti(jti);
        return session.isPresent() && !session.get().isRevoked() && session.get().getExpiresAt().isAfter(Instant.now());
    }

    @Transactional
    public void touchSession(String jti) {
        userSessionRepository.findByJti(jti).ifPresent(session -> {
            if (!session.isRevoked()) {
                session.setRevoked(false);
                userSessionRepository.save(session);
            }
        });
    }

    @Transactional
    public void revokeAllSessions(Long userId) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(userId, Instant.now());
        for (UserSession session : sessions) {
            session.setRevoked(true);
        }
        userSessionRepository.saveAll(sessions);
    }

    @Transactional
    public void revokeOtherSessions(Long userId, Long currentSessionId) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(userId, Instant.now());
        for (UserSession session : sessions) {
            if (!session.getId().equals(currentSessionId)) {
                session.setRevoked(true);
            }
        }
        userSessionRepository.saveAll(sessions);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
