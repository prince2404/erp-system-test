package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByCenterIdAndAppointmentDate(Long centerId, LocalDate appointmentDate);
}
