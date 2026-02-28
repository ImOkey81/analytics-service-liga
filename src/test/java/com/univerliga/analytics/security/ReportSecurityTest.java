package com.univerliga.analytics.security;

import com.univerliga.analytics.config.AnalyticsProperties;
import com.univerliga.analytics.config.ProvisioningProperties;
import com.univerliga.analytics.controller.ReportController;
import com.univerliga.analytics.error.GlobalExceptionHandler;
import com.univerliga.analytics.export.ExportService;
import com.univerliga.analytics.service.ReportService;
import com.univerliga.analytics.util.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, RequestIdFilter.class, ReportSecurityTest.TestConfig.class})
class ReportSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void employeeShouldGet403OnSummary() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-03-31")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_EMPLOYEE")))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        ReportService reportService() {
            return mock(ReportService.class);
        }

        @Bean
        ExportService exportService() {
            return mock(ExportService.class);
        }

        @Bean
        ProvisioningProperties provisioningProperties() {
            return new ProvisioningProperties(ProvisioningProperties.Mode.mock);
        }

        @Bean
        AnalyticsProperties analyticsProperties() {
            return new AnalyticsProperties(new AnalyticsProperties.Features(false));
        }
    }
}
