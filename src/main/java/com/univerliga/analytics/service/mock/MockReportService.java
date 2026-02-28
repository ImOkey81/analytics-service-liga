package com.univerliga.analytics.service.mock;

import com.univerliga.analytics.dto.*;
import com.univerliga.analytics.error.ApiException;
import com.univerliga.analytics.model.FeedbackRecord;
import com.univerliga.analytics.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MockReportService implements ReportService {

    private static final Set<String> TREND_METRICS = Set.of("responses", "avgRating", "positiveShare", "negativeShare");
    private static final Set<String> GRANULARITY = Set.of("month", "week");

    private final List<FeedbackRecord> feedback;

    public MockReportService(MockDataFactory factory) {
        this.feedback = factory.feedbacks();
    }

    @Override
    public SummaryResponse summary(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        List<FeedbackRecord> filtered = filter(from, to, departmentId, teamId, personId, null);
        return new SummaryResponse(period(from, to), scope(departmentId, teamId, personId, null), toKpis(filtered));
    }

    @Override
    public PositivityByPersonResponse positivityByPerson(LocalDate from, LocalDate to, String departmentId, String teamId, int limit, String sort) {
        Comparator<PositivityByPersonResponse.Item> comparator = switch (sort == null ? "total" : sort) {
            case "positive" -> Comparator.comparingLong(PositivityByPersonResponse.Item::positive).reversed();
            case "negative" -> Comparator.comparingLong(PositivityByPersonResponse.Item::negative).reversed();
            case "avgRating" -> Comparator.comparingDouble(PositivityByPersonResponse.Item::avgRating).reversed();
            default -> Comparator.comparingLong(PositivityByPersonResponse.Item::total).reversed();
        };

        List<PositivityByPersonResponse.Item> items = filter(from, to, departmentId, teamId, null, null).stream()
                .collect(Collectors.groupingBy(FeedbackRecord::targetId))
                .entrySet().stream()
                .map(entry -> {
                    List<FeedbackRecord> values = entry.getValue();
                    FeedbackRecord sample = values.getFirst();
                    long positive = values.stream().filter(v -> v.rating() >= 4).count();
                    long negative = values.stream().filter(v -> v.rating() <= 2).count();
                    long total = positive + negative;
                    double avg = values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0);
                    return new PositivityByPersonResponse.Item(entry.getKey(), sample.targetName(), positive, negative, total, round(avg));
                })
                .sorted(comparator)
                .limit(limit)
                .toList();

        return new PositivityByPersonResponse(period(from, to), scope(departmentId, teamId, null, null), items);
    }

    @Override
    public SubcategoryFrequencyResponse subcategoryFrequency(LocalDate from, LocalDate to, String departmentId, String teamId, String personId, String categoryId, int limit, String sort) {
        Comparator<SubcategoryFrequencyResponse.Item> comparator = switch (sort == null ? "total" : sort) {
            case "positive" -> Comparator.comparingLong(SubcategoryFrequencyResponse.Item::positive).reversed();
            case "negative" -> Comparator.comparingLong(SubcategoryFrequencyResponse.Item::negative).reversed();
            default -> Comparator.comparingLong(SubcategoryFrequencyResponse.Item::total).reversed();
        };

        List<SubcategoryFrequencyResponse.Item> items = filter(from, to, departmentId, teamId, personId, categoryId).stream()
                .collect(Collectors.groupingBy(FeedbackRecord::subcategoryId))
                .entrySet().stream()
                .map(entry -> {
                    List<FeedbackRecord> values = entry.getValue();
                    FeedbackRecord sample = values.getFirst();
                    long positive = values.stream().filter(v -> v.rating() >= 4).count();
                    long negative = values.stream().filter(v -> v.rating() <= 2).count();
                    return new SubcategoryFrequencyResponse.Item(sample.subcategoryId(), sample.subcategoryName(), positive, negative, positive + negative);
                })
                .sorted(comparator)
                .limit(limit)
                .toList();

        return new SubcategoryFrequencyResponse(period(from, to), scope(departmentId, teamId, personId, categoryId), items);
    }

    @Override
    public TrendResponse trend(String metric, String granularity, LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        if (!TREND_METRICS.contains(metric)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Unsupported metric");
        }
        if (!GRANULARITY.contains(granularity)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Unsupported granularity");
        }

        Map<String, List<FeedbackRecord>> buckets = filter(from, to, departmentId, teamId, personId, null).stream()
                .collect(Collectors.groupingBy(f -> bucketKey(f.date(), granularity), TreeMap::new, Collectors.toList()));

        List<TrendResponse.Point> points = buckets.entrySet().stream()
                .map(e -> new TrendResponse.Point(e.getKey(), metricValue(metric, e.getValue())))
                .toList();

        return new TrendResponse(metric, granularity, period(from, to), points);
    }

    @Override
    public RatingsByCategoryResponse ratingsByCategory(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        List<RatingsByCategoryResponse.Item> series = filter(from, to, departmentId, teamId, personId, null).stream()
                .collect(Collectors.groupingBy(FeedbackRecord::categoryId))
                .entrySet().stream()
                .map(entry -> {
                    List<FeedbackRecord> values = entry.getValue();
                    FeedbackRecord sample = values.getFirst();
                    return new RatingsByCategoryResponse.Item(
                            sample.categoryId(),
                            sample.categoryName(),
                            round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)),
                            values.size());
                })
                .sorted(Comparator.comparingDouble(RatingsByCategoryResponse.Item::avgRating).reversed())
                .toList();
        return new RatingsByCategoryResponse(series);
    }

    @Override
    public DashboardResponse dashboard(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        SummaryResponse summary = summary(from, to, departmentId, teamId, personId);
        RatingsByCategoryResponse ratings = ratingsByCategory(from, to, departmentId, teamId, personId);
        TrendResponse trend = trend("responses", "month", from, to, departmentId, teamId, personId);
        PositivityByPersonResponse positivity = positivityByPerson(from, to, departmentId, teamId, 5, "total");
        SubcategoryFrequencyResponse subcategory = subcategoryFrequency(from, to, departmentId, teamId, personId, null, 5, "total");
        return new DashboardResponse(
                period(from, to),
                scope(departmentId, teamId, personId, null),
                summary.kpis(),
                new DashboardResponse.Charts(ratings.series(), new DashboardResponse.TrendChart("responses", "month", trend.points()), positivity.items(), subcategory.items())
        );
    }

    @Override
    public InsightsResponse insights(LocalDate from, LocalDate to, String departmentId, String teamId, int limit) {
        Map<String, List<FeedbackRecord>> grouped = filter(from, to, departmentId, teamId, null, null).stream()
                .collect(Collectors.groupingBy(FeedbackRecord::subcategoryId));

        List<InsightsResponse.Item> items = grouped.values().stream().map(values -> {
            FeedbackRecord sample = values.getFirst();
            return new InsightsResponse.Item(sample.subcategoryId(), sample.subcategoryName(), round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)), values.size());
        }).toList();

        List<InsightsResponse.Item> best = items.stream().sorted(Comparator.comparingDouble(InsightsResponse.Item::avgRating).reversed()).limit(limit).toList();
        List<InsightsResponse.Item> worst = items.stream().sorted(Comparator.comparingDouble(InsightsResponse.Item::avgRating)).limit(limit).toList();
        return new InsightsResponse(period(from, to), best, worst);
    }

    @Override
    public NetworkResponse network(LocalDate from, LocalDate to, String teamId) {
        List<FeedbackRecord> filtered = filter(from, to, null, teamId, null, null);
        List<NetworkResponse.Node> nodes = filtered.stream()
                .map(f -> new NetworkResponse.Node(f.targetId(), f.targetName(), f.teamId()))
                .distinct()
                .toList();

        Map<String, List<FeedbackRecord>> edgesGrouped = filtered.stream().collect(Collectors.groupingBy(f -> f.authorId() + "->" + f.targetId()));
        List<NetworkResponse.Edge> edges = edgesGrouped.values().stream().map(values -> {
            FeedbackRecord first = values.getFirst();
            return new NetworkResponse.Edge(
                    first.authorId(),
                    first.targetId(),
                    values.size(),
                    round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)),
                    values.stream().filter(v -> v.rating() >= 4).count(),
                    values.stream().filter(v -> v.rating() <= 2).count());
        }).toList();

        return new NetworkResponse(period(from, to), nodes, edges);
    }

    private SummaryResponse.Kpis toKpis(List<FeedbackRecord> records) {
        long positive = records.stream().filter(v -> v.rating() >= 4).count();
        long negative = records.stream().filter(v -> v.rating() <= 2).count();
        long total = positive + negative;
        return new SummaryResponse.Kpis(
                records.size(),
                records.stream().map(FeedbackRecord::authorId).distinct().count(),
                records.stream().map(FeedbackRecord::targetId).distinct().count(),
                round(records.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)),
                total == 0 ? 0 : round((double) positive / total),
                total == 0 ? 0 : round((double) negative / total)
        );
    }

    private List<FeedbackRecord> filter(LocalDate from, LocalDate to, String departmentId, String teamId, String personId, String categoryId) {
        return feedback.stream()
                .filter(f -> !f.date().isBefore(from) && !f.date().isAfter(to))
                .filter(f -> departmentId == null || departmentId.isBlank() || departmentId.equals(f.departmentId()))
                .filter(f -> teamId == null || teamId.isBlank() || teamId.equals(f.teamId()))
                .filter(f -> personId == null || personId.isBlank() || personId.equals(f.targetId()))
                .filter(f -> categoryId == null || categoryId.isBlank() || categoryId.equals(f.categoryId()))
                .toList();
    }

    private String bucketKey(LocalDate date, String granularity) {
        if ("month".equals(granularity)) {
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        }
        WeekFields wf = WeekFields.ISO;
        return String.format("%d-W%02d", date.getYear(), date.get(wf.weekOfWeekBasedYear()));
    }

    private double metricValue(String metric, List<FeedbackRecord> values) {
        long positive = values.stream().filter(v -> v.rating() >= 4).count();
        long negative = values.stream().filter(v -> v.rating() <= 2).count();
        long total = positive + negative;
        return switch (metric) {
            case "responses" -> values.size();
            case "avgRating" -> round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0));
            case "positiveShare" -> total == 0 ? 0.0 : round((double) positive / total);
            case "negativeShare" -> total == 0 ? 0.0 : round((double) negative / total);
            default -> 0.0;
        };
    }

    private PeriodDto period(LocalDate from, LocalDate to) {
        return new PeriodDto(from.toString(), to.toString());
    }

    private ScopeDto scope(String departmentId, String teamId, String personId, String categoryId) {
        return new ScopeDto(emptyToNull(departmentId), emptyToNull(teamId), emptyToNull(personId), emptyToNull(categoryId));
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
