package com.univerliga.analytics.dto;

import java.util.List;

public record InsightsResponse(PeriodDto period, List<Item> best, List<Item> worst) {
    public record Item(String subcategoryId, String name, double avgRating, long count) {
    }
}
