package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
