package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Optional<Diagnosis> findByAppointmentId(Long appointmentId);
}
