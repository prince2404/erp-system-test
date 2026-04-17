package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
}
