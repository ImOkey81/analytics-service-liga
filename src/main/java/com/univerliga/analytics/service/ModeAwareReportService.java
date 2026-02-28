package com.univerliga.analytics.service;

import com.univerliga.analytics.config.ProvisioningProperties;
import com.univerliga.analytics.dto.*;
import com.univerliga.analytics.service.mock.MockReportService;
import com.univerliga.analytics.service.readmodel.ReadModelReportService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Primary
public class ModeAwareReportService implements ReportService {

    private final ProvisioningProperties provisioningProperties;
    private final MockReportService mockReportService;
    private final ReadModelReportService readModelReportService;

    public ModeAwareReportService(ProvisioningProperties provisioningProperties, MockReportService mockReportService, ReadModelReportService readModelReportService) {
        this.provisioningProperties = provisioningProperties;
        this.mockReportService = mockReportService;
        this.readModelReportService = readModelReportService;
    }

    private ReportService delegate() {
        return provisioningProperties.mode() == ProvisioningProperties.Mode.readmodel ? readModelReportService : mockReportService;
    }

    @Override
    public SummaryResponse summary(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        return delegate().summary(from, to, departmentId, teamId, personId);
    }

    @Override
    public PositivityByPersonResponse positivityByPerson(LocalDate from, LocalDate to, String departmentId, String teamId, int limit, String sort) {
        return delegate().positivityByPerson(from, to, departmentId, teamId, limit, sort);
    }

    @Override
    public SubcategoryFrequencyResponse subcategoryFrequency(LocalDate from, LocalDate to, String departmentId, String teamId, String personId, String categoryId, int limit, String sort) {
        return delegate().subcategoryFrequency(from, to, departmentId, teamId, personId, categoryId, limit, sort);
    }

    @Override
    public TrendResponse trend(String metric, String granularity, LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        return delegate().trend(metric, granularity, from, to, departmentId, teamId, personId);
    }

    @Override
    public RatingsByCategoryResponse ratingsByCategory(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        return delegate().ratingsByCategory(from, to, departmentId, teamId, personId);
    }

    @Override
    public DashboardResponse dashboard(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        return delegate().dashboard(from, to, departmentId, teamId, personId);
    }

    @Override
    public InsightsResponse insights(LocalDate from, LocalDate to, String departmentId, String teamId, int limit) {
        return delegate().insights(from, to, departmentId, teamId, limit);
    }

    @Override
    public NetworkResponse network(LocalDate from, LocalDate to, String teamId) {
        return delegate().network(from, to, teamId);
    }
}
