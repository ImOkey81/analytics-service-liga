package com.univerliga.analytics.dto;

import java.util.List;

public record RatingsByCategoryResponse(List<Item> series) {
    public record Item(String categoryId, String categoryName, double avgRating, long count) {
    }
}
