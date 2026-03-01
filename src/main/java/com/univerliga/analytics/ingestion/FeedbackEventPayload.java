package com.univerliga.analytics.ingestion;

import java.time.LocalDate;

public record FeedbackEventPayload(
        String feedbackId,
        String authorPersonId,
        String targetPersonId,
        String targetName,
        String departmentId,
        String teamId,
        String categoryId,
        String categoryName,
        String subcategoryId,
        String subcategoryName,
        Integer rating,
        LocalDate feedbackDate
) {
}
