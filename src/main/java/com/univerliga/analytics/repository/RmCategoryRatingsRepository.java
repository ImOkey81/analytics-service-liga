package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmCategoryRatingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmCategoryRatingsRepository extends JpaRepository<RmCategoryRatingsEntity, Long> {
    List<RmCategoryRatingsEntity> findByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId, String personId);

    void deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId);
}
