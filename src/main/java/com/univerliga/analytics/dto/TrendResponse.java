package com.univerliga.analytics.dto;

import java.util.List;

public record TrendResponse(String metric, String granularity, PeriodDto period, List<Point> points) {
    public record Point(String x, double y) {
    }
}
