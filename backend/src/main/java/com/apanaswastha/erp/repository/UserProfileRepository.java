package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
