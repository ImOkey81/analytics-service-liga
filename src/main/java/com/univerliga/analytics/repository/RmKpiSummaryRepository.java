package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmKpiSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RmKpiSummaryRepository extends JpaRepository<RmKpiSummaryEntity, Long> {
    Optional<RmKpiSummaryEntity> findFirstByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId, String personId);
}
