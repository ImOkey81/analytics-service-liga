package com.univerliga.analytics.dto;

import java.util.List;

public record PositivityByPersonResponse(PeriodDto period, ScopeDto scope, List<Item> items) {
    public record Item(String personId, String displayName, long positive, long negative, long total, double avgRating) {
    }
}
