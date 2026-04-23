package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.UserPermissionToggle;
import com.apanaswastha.erp.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionToggleRepository extends JpaRepository<UserPermissionToggle, Long> {
    List<UserPermissionToggle> findByUserId(Long userId);
    Optional<UserPermissionToggle> findByUserIdAndTargetRole(Long userId, RoleName targetRole);
}
