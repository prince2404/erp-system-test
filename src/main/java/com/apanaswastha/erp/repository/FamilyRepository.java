package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findByHealthCardNumber(String healthCardNumber);
}
