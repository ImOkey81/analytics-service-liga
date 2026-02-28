package com.univerliga.analytics.dto;

import java.util.List;

public record DashboardResponse(
        PeriodDto period,
        ScopeDto scope,
        SummaryResponse.Kpis kpis,
        Charts charts
) {
    public record Charts(
            List<RatingsByCategoryResponse.Item> ratingsByCategory,
            TrendChart trend,
            List<PositivityByPersonResponse.Item> positivityByPerson,
            List<SubcategoryFrequencyResponse.Item> subcategoryFrequency
    ) {
    }

    public record TrendChart(String metric, String granularity, List<TrendResponse.Point> points) {
    }
}
