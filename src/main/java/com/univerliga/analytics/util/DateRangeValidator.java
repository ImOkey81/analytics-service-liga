package com.univerliga.analytics.util;

import com.univerliga.analytics.error.ApiException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public final class DateRangeValidator {

    private DateRangeValidator() {
    }

    public static void validate(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "periodFrom/periodTo are required");
        }
        if (from.isAfter(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "periodFrom must be <= periodTo");
        }
    }
}
