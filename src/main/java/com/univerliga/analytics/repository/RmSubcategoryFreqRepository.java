package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmSubcategoryFreqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmSubcategoryFreqRepository extends JpaRepository<RmSubcategoryFreqEntity, Long> {
    List<RmSubcategoryFreqEntity> findByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonIdAndCategoryId(LocalDate periodFrom, LocalDate periodTo, String departmentId, String teamId, String personId, String categoryId);
}
