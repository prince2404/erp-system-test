package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Center;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface CenterRepository extends JpaRepository<Center, Long> {

    @Query("""
            select count(c)
            from Center c
            where (:stateId is null or c.block.district.state.id = :stateId)
              and (:districtId is null or c.block.district.id = :districtId)
              and (:blockId is null or c.block.id = :blockId)
              and (:centerId is null or c.id = :centerId)
            """)
    long countByScope(
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("blockId") Long blockId,
            @Param("centerId") Long centerId
    );
}
