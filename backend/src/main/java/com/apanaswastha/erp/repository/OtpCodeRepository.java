package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.OtpCode;
import com.apanaswastha.erp.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, OtpType type);
    Optional<OtpCode> findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(Long userId, OtpType type);
}
