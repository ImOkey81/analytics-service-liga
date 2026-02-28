package com.univerliga.analytics.service;

import com.univerliga.analytics.dto.*;

import java.time.LocalDate;

public interface ReportService {

    SummaryResponse summary(LocalDate from, LocalDate to, String departmentId, String teamId, String personId);

    PositivityByPersonResponse positivityByPerson(LocalDate from, LocalDate to, String departmentId, String teamId, int limit, String sort);

    SubcategoryFrequencyResponse subcategoryFrequency(LocalDate from, LocalDate to, String departmentId, String teamId, String personId, String categoryId, int limit, String sort);

    TrendResponse trend(String metric, String granularity, LocalDate from, LocalDate to, String departmentId, String teamId, String personId);

    RatingsByCategoryResponse ratingsByCategory(LocalDate from, LocalDate to, String departmentId, String teamId, String personId);

    DashboardResponse dashboard(LocalDate from, LocalDate to, String departmentId, String teamId, String personId);

    InsightsResponse insights(LocalDate from, LocalDate to, String departmentId, String teamId, int limit);

    NetworkResponse network(LocalDate from, LocalDate to, String teamId);
}
