package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
}
