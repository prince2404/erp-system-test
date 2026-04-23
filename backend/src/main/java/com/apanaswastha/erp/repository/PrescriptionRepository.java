package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByDiagnosisAppointmentIdAndDispensedTrue(Long appointmentId);
}
