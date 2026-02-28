package com.univerliga.analytics.dto;

public record SummaryResponse(PeriodDto period, ScopeDto scope, Kpis kpis) {
    public record Kpis(long responses, long uniqueAuthors, long uniqueTargets, double avgRating, double positiveShare, double negativeShare) {
    }
}
