package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmFeedbackEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmFeedbackEventRepository extends JpaRepository<RmFeedbackEventEntity, String> {

    List<RmFeedbackEventEntity> findByFeedbackDateBetweenAndDepartmentId(LocalDate from, LocalDate to, String departmentId);

    List<RmFeedbackEventEntity> findByFeedbackDateBetweenAndDepartmentIdAndTeamId(LocalDate from, LocalDate to, String departmentId, String teamId);
}
