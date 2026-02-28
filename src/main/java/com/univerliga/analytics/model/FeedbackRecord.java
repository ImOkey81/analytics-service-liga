package com.univerliga.analytics.model;

import java.time.LocalDate;

public record FeedbackRecord(
        String id,
        String authorId,
        String targetId,
        String targetName,
        String departmentId,
        String teamId,
        String categoryId,
        String categoryName,
        String subcategoryId,
        String subcategoryName,
        int rating,
        LocalDate date
) {
}
