package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmPersonPositivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmPersonPositivityRepository extends JpaRepository<RmPersonPositivityEntity, Long> {
    List<RmPersonPositivityEntity> findByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId);

    void deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId);
}
