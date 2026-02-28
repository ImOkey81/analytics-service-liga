package com.univerliga.analytics.controller;

import com.univerliga.analytics.config.AnalyticsProperties;
import com.univerliga.analytics.dto.*;
import com.univerliga.analytics.error.ApiException;
import com.univerliga.analytics.export.ExportFormat;
import com.univerliga.analytics.export.ExportMode;
import com.univerliga.analytics.export.ExportService;
import com.univerliga.analytics.service.ReportService;
import com.univerliga.analytics.util.DateRangeValidator;
import com.univerliga.analytics.util.RequestIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping(path = "/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;
    private final AnalyticsProperties analyticsProperties;

    public ReportController(ReportService reportService, ExportService exportService, AnalyticsProperties analyticsProperties) {
        this.reportService = reportService;
        this.exportService = exportService;
        this.analyticsProperties = analyticsProperties;
    }

    @GetMapping("/summary")
    @Operation(summary = "KPI summary", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
            content = @Content(examples = @ExampleObject(value = "{\"data\":{\"period\":{\"from\":\"2026-01-01\",\"to\":\"2026-02-28\"},\"scope\":{\"departmentId\":\"d_1\",\"teamId\":\"t_2\",\"personId\":null,\"categoryId\":null},\"kpis\":{\"responses\":120,\"uniqueAuthors\":34,\"uniqueTargets\":18,\"avgRating\":4.2,\"positiveShare\":0.76,\"negativeShare\":0.24}},\"meta\":{\"requestId\":\"req-1\",\"timestamp\":\"2026-01-01T10:00:00Z\",\"version\":\"v1\"}}"))))
    public ApiResponse<SummaryResponse> summary(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String personId) {
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.summary(periodFrom, periodTo, departmentId, teamId, personId), RequestIdHolder.get());
    }

    @GetMapping("/charts/positivity-by-person")
    public ApiResponse<PositivityByPersonResponse> positivityByPerson(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "total") String sort) {
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.positivityByPerson(periodFrom, periodTo, departmentId, teamId, limit, sort), RequestIdHolder.get());
    }

    @GetMapping("/charts/subcategory-frequency")
    public ApiResponse<SubcategoryFrequencyResponse> subcategoryFrequency(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String personId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "30") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "total") String sort) {
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.subcategoryFrequency(periodFrom, periodTo, departmentId, teamId, personId, categoryId, limit, sort), RequestIdHolder.get());
    }

    @GetMapping("/charts/trend")
    public ApiResponse<TrendResponse> trend(
            @RequestParam String metric,
            @RequestParam(defaultValue = "month") String granularity,
            @RequestParam("from") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String personId) {
        DateRangeValidator.validate(from, to);
        return ApiResponse.of(reportService.trend(metric, granularity, from, to, departmentId, teamId, personId), RequestIdHolder.get());
    }

    @GetMapping("/charts/ratings-by-category")
    public ApiResponse<RatingsByCategoryResponse> ratingsByCategory(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String personId) {
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.ratingsByCategory(periodFrom, periodTo, departmentId, teamId, personId), RequestIdHolder.get());
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String personId) {
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.dashboard(periodFrom, periodTo, departmentId, teamId, personId), RequestIdHolder.get());
    }

    @GetMapping("/insights/top-subcategories")
    public ApiResponse<InsightsResponse> insights(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(200) int limit) {
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.insights(periodFrom, periodTo, departmentId, teamId, limit), RequestIdHolder.get());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam ExportFormat format,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String personId,
            @RequestParam(defaultValue = "aggregated") ExportMode mode) {
        DateRangeValidator.validate(periodFrom, periodTo);
        byte[] body = exportService.export(format, periodFrom, periodTo, departmentId, teamId, personId, mode);
        MediaType contentType = format == ExportFormat.csv ? MediaType.valueOf("text/csv") : MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "analytics." + format.name();
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .body(body);
    }

    @GetMapping("/network/interactions")
    public ApiResponse<NetworkResponse> network(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String teamId) {
        if (!analyticsProperties.features().network()) {
            throw new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "NOT_FOUND", "Endpoint not found");
        }
        DateRangeValidator.validate(periodFrom, periodTo);
        return ApiResponse.of(reportService.network(periodFrom, periodTo, teamId), RequestIdHolder.get());
    }
}
