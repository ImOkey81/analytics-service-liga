package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmTrendPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmTrendPointRepository extends JpaRepository<RmTrendPointEntity, Long> {
    List<RmTrendPointEntity> findByMetricAndGranularityAndPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(
            String metric,
            String granularity,
            LocalDate periodFrom,
            LocalDate periodTo,
            String departmentId,
            String teamId,
            String personId
    );

    void deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId);
}
