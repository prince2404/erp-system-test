package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Appointment;
import com.apanaswastha.erp.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByCenterIdAndAppointmentDate(Long centerId, LocalDate appointmentDate);

    @Query("""
            select count(a)
            from Appointment a
            where a.appointmentDate = :appointmentDate
              and (:stateId is null or a.center.block.district.state.id = :stateId)
              and (:districtId is null or a.center.block.district.id = :districtId)
              and (:blockId is null or a.center.block.id = :blockId)
              and (:centerId is null or a.center.id = :centerId)
            """)
    long countByAppointmentDateAndScope(
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("blockId") Long blockId,
            @Param("centerId") Long centerId
    );

    List<Appointment> findByDoctorIdAndStatusOrderByCreatedAtAsc(Long doctorId, AppointmentStatus status);

    List<Appointment> findByDoctorIdOrderByCreatedAtAsc(Long doctorId);

    List<Appointment> findByStatusOrderByCreatedAtAsc(AppointmentStatus status);

    List<Appointment> findAllByOrderByCreatedAtDesc();
}
