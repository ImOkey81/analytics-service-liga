package com.univerliga.analytics.dto;

import java.util.List;

public record SubcategoryFrequencyResponse(PeriodDto period, ScopeDto scope, List<Item> items) {
    public record Item(String subcategoryId, String subcategoryName, long positive, long negative, long total) {
    }
}
