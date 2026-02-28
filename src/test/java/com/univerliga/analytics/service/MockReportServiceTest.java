package com.univerliga.analytics.service;

import com.univerliga.analytics.dto.SummaryResponse;
import com.univerliga.analytics.dto.TrendResponse;
import com.univerliga.analytics.service.mock.MockDataFactory;
import com.univerliga.analytics.service.mock.MockReportService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MockReportServiceTest {

    private final MockReportService service = new MockReportService(new MockDataFactory());

    @Test
    void summaryShouldReturnAggregatedKpis() {
        SummaryResponse response = service.summary(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), null, null, null);

        assertThat(response.kpis().responses()).isPositive();
        assertThat(response.kpis().uniqueAuthors()).isPositive();
        assertThat(response.kpis().avgRating()).isBetween(1.0, 5.0);
    }

    @Test
    void trendShouldReturnMonthlyPoints() {
        TrendResponse response = service.trend("responses", "month", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), null, null, null);

        assertThat(response.points()).isNotEmpty();
        assertThat(response.points().getFirst().x()).startsWith("2026-");
        assertThat(response.points().getFirst().y()).isPositive();
    }
}
