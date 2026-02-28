package com.univerliga.analytics.dto;

import java.util.List;

public record NetworkResponse(PeriodDto period, List<Node> nodes, List<Edge> edges) {
    public record Node(String id, String label, String teamId) {
    }

    public record Edge(String source, String target, long weight, double avgRating, long positive, long negative) {
    }
}
