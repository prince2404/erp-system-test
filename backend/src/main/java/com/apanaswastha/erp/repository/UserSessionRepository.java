package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByJti(String jti);
    Optional<UserSession> findByJtiAndUserId(String jti, Long userId);
    List<UserSession> findByUserIdAndRevokedFalseAndExpiresAtAfter(Long userId, Instant now);
    Optional<UserSession> findByIdAndUserId(Long id, Long userId);
}
