package com.univerliga.analytics.ingestion;

import com.univerliga.analytics.model.FeedbackRecord;
import com.univerliga.analytics.model.RmCategoryRatingsEntity;
import com.univerliga.analytics.model.RmFeedbackEventEntity;
import com.univerliga.analytics.model.RmInsightsEntity;
import com.univerliga.analytics.model.RmKpiSummaryEntity;
import com.univerliga.analytics.model.RmNetworkEdgeEntity;
import com.univerliga.analytics.model.RmNetworkNodeEntity;
import com.univerliga.analytics.model.RmPersonPositivityEntity;
import com.univerliga.analytics.model.RmSubcategoryFreqEntity;
import com.univerliga.analytics.model.RmTrendPointEntity;
import com.univerliga.analytics.repository.RmCategoryRatingsRepository;
import com.univerliga.analytics.repository.RmFeedbackEventRepository;
import com.univerliga.analytics.repository.RmInsightsRepository;
import com.univerliga.analytics.repository.RmKpiSummaryRepository;
import com.univerliga.analytics.repository.RmNetworkEdgeRepository;
import com.univerliga.analytics.repository.RmNetworkNodeRepository;
import com.univerliga.analytics.repository.RmPersonPositivityRepository;
import com.univerliga.analytics.repository.RmSubcategoryFreqRepository;
import com.univerliga.analytics.repository.RmTrendPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncrementalReadModelProjector {

    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;
    private static final Set<String> TREND_METRICS = Set.of("responses", "avgRating", "positiveShare", "negativeShare");

    private final RmFeedbackEventRepository feedbackEventRepository;
    private final RmKpiSummaryRepository kpiSummaryRepository;
    private final RmPersonPositivityRepository personPositivityRepository;
    private final RmSubcategoryFreqRepository subcategoryFreqRepository;
    private final RmCategoryRatingsRepository categoryRatingsRepository;
    private final RmTrendPointRepository trendPointRepository;
    private final RmInsightsRepository insightsRepository;
    private final RmNetworkNodeRepository networkNodeRepository;
    private final RmNetworkEdgeRepository networkEdgeRepository;

    @Transactional
    public void reproject(LocalDate periodFrom, LocalDate periodTo, Set<ProjectionScope> scopes) {
        if (scopes.isEmpty()) {
            return;
        }

        Set<String> departments = new HashSet<>();
        for (ProjectionScope scope : scopes) {
            departments.add(scope.departmentId());
            reprojectTeam(periodFrom, periodTo, scope.departmentId(), scope.teamId());
        }
        for (String departmentId : departments) {
            reprojectDepartmentGlobal(periodFrom, periodTo, departmentId);
        }
    }

    private void reprojectTeam(LocalDate from, LocalDate to, String departmentId, String teamId) {
        List<FeedbackRecord> feedback = feedbackEventRepository
                .findByFeedbackDateBetweenAndDepartmentIdAndTeamId(from, to, departmentId, teamId)
                .stream()
                .map(this::toFeedbackRecord)
                .toList();

        clearTeamScope(from, to, departmentId, teamId);
        if (feedback.isEmpty()) {
            return;
        }

        kpiSummaryRepository.save(toKpi(from, to, departmentId, teamId, null, feedback));
        feedback.stream().collect(Collectors.groupingBy(FeedbackRecord::targetId))
                .forEach((personId, values) -> kpiSummaryRepository.save(toKpi(from, to, departmentId, teamId, personId, values)));

        personPositivityRepository.saveAll(buildPersonPositivity(from, to, departmentId, teamId, feedback));
        subcategoryFreqRepository.saveAll(buildSubcategoryFreq(from, to, departmentId, teamId, feedback));
        categoryRatingsRepository.saveAll(buildCategoryRatings(from, to, departmentId, teamId, feedback));
        trendPointRepository.saveAll(buildTrendPoints(from, to, departmentId, teamId, feedback));
        insightsRepository.saveAll(buildInsights(from, to, departmentId, teamId, feedback));
        networkNodeRepository.saveAll(buildNetworkNodes(from, to, teamId, feedback));
        networkEdgeRepository.saveAll(buildNetworkEdges(from, to, teamId, feedback));
    }

    private void reprojectDepartmentGlobal(LocalDate from, LocalDate to, String departmentId) {
        List<FeedbackRecord> feedback = feedbackEventRepository
                .findByFeedbackDateBetweenAndDepartmentId(from, to, departmentId)
                .stream()
                .map(this::toFeedbackRecord)
                .toList();

        kpiSummaryRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(from, to, departmentId, null, null);
        if (!feedback.isEmpty()) {
            kpiSummaryRepository.save(toKpi(from, to, departmentId, null, null, feedback));
        }
    }

    private void clearTeamScope(LocalDate from, LocalDate to, String departmentId, String teamId) {
        networkEdgeRepository.deleteByPeriodFromAndPeriodToAndTeamId(from, to, teamId);
        networkNodeRepository.deleteByPeriodFromAndPeriodToAndTeamId(from, to, teamId);
        insightsRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, departmentId, teamId);
        trendPointRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, departmentId, teamId);
        categoryRatingsRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, departmentId, teamId);
        subcategoryFreqRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, departmentId, teamId);
        personPositivityRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, departmentId, teamId);
        kpiSummaryRepository.deleteByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, departmentId, teamId);
    }

    private RmKpiSummaryEntity toKpi(LocalDate from, LocalDate to, String departmentId, String teamId, String personId, List<FeedbackRecord> values) {
        long positive = values.stream().filter(v -> v.rating() >= 4).count();
        long negative = values.stream().filter(v -> v.rating() <= 2).count();
        long total = positive + negative;

        RmKpiSummaryEntity row = new RmKpiSummaryEntity();
        row.setPeriodFrom(from);
        row.setPeriodTo(to);
        row.setDepartmentId(departmentId);
        row.setTeamId(teamId);
        row.setPersonId(personId);
        row.setResponses(values.size());
        row.setUniqueAuthors(values.stream().map(FeedbackRecord::authorId).distinct().count());
        row.setUniqueTargets(values.stream().map(FeedbackRecord::targetId).distinct().count());
        row.setAvgRating(round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)));
        row.setPositiveShare(total == 0 ? 0.0 : round((double) positive / total));
        row.setNegativeShare(total == 0 ? 0.0 : round((double) negative / total));
        return row;
    }

    private List<RmPersonPositivityEntity> buildPersonPositivity(LocalDate from, LocalDate to, String departmentId, String teamId, List<FeedbackRecord> feedback) {
        return feedback.stream().collect(Collectors.groupingBy(FeedbackRecord::targetId))
                .values()
                .stream()
                .map(values -> {
                    FeedbackRecord sample = values.getFirst();
                    long positive = values.stream().filter(v -> v.rating() >= 4).count();
                    long negative = values.stream().filter(v -> v.rating() <= 2).count();
                    RmPersonPositivityEntity row = new RmPersonPositivityEntity();
                    row.setPeriodFrom(from);
                    row.setPeriodTo(to);
                    row.setDepartmentId(departmentId);
                    row.setTeamId(teamId);
                    row.setPersonId(sample.targetId());
                    row.setDisplayName(sample.targetName());
                    row.setPositive(positive);
                    row.setNegative(negative);
                    row.setTotal(positive + negative);
                    row.setAvgRating(round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)));
                    return row;
                })
                .toList();
    }

    private List<RmSubcategoryFreqEntity> buildSubcategoryFreq(LocalDate from, LocalDate to, String departmentId, String teamId, List<FeedbackRecord> feedback) {
        List<RmSubcategoryFreqEntity> out = new ArrayList<>();
        appendSubcategoryRows(out, from, to, departmentId, teamId, null, feedback);
        feedback.stream().collect(Collectors.groupingBy(FeedbackRecord::targetId))
                .forEach((personId, values) -> appendSubcategoryRows(out, from, to, departmentId, teamId, personId, values));
        return out;
    }

    private void appendSubcategoryRows(List<RmSubcategoryFreqEntity> out, LocalDate from, LocalDate to, String departmentId, String teamId, String personId, List<FeedbackRecord> values) {
        values.stream().collect(Collectors.groupingBy(v -> v.categoryId() + "::" + v.subcategoryId()))
                .values()
                .forEach(grouped -> {
                    FeedbackRecord sample = grouped.getFirst();
                    long positive = grouped.stream().filter(v -> v.rating() >= 4).count();
                    long negative = grouped.stream().filter(v -> v.rating() <= 2).count();

                    RmSubcategoryFreqEntity row = new RmSubcategoryFreqEntity();
                    row.setPeriodFrom(from);
                    row.setPeriodTo(to);
                    row.setDepartmentId(departmentId);
                    row.setTeamId(teamId);
                    row.setPersonId(personId);
                    row.setCategoryId(sample.categoryId());
                    row.setSubcategoryId(sample.subcategoryId());
                    row.setSubcategoryName(sample.subcategoryName());
                    row.setPositive(positive);
                    row.setNegative(negative);
                    row.setTotal(positive + negative);
                    out.add(row);
                });
    }

    private List<RmCategoryRatingsEntity> buildCategoryRatings(LocalDate from, LocalDate to, String departmentId, String teamId, List<FeedbackRecord> feedback) {
        List<RmCategoryRatingsEntity> out = new ArrayList<>();
        appendCategoryRows(out, from, to, departmentId, teamId, null, feedback);
        feedback.stream().collect(Collectors.groupingBy(FeedbackRecord::targetId))
                .forEach((personId, values) -> appendCategoryRows(out, from, to, departmentId, teamId, personId, values));
        return out;
    }

    private void appendCategoryRows(List<RmCategoryRatingsEntity> out, LocalDate from, LocalDate to, String departmentId, String teamId, String personId, List<FeedbackRecord> values) {
        values.stream().collect(Collectors.groupingBy(FeedbackRecord::categoryId))
                .values()
                .forEach(grouped -> {
                    FeedbackRecord sample = grouped.getFirst();
                    RmCategoryRatingsEntity row = new RmCategoryRatingsEntity();
                    row.setPeriodFrom(from);
                    row.setPeriodTo(to);
                    row.setDepartmentId(departmentId);
                    row.setTeamId(teamId);
                    row.setPersonId(personId);
                    row.setCategoryId(sample.categoryId());
                    row.setCategoryName(sample.categoryName());
                    row.setAvgRating(round(grouped.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)));
                    row.setCount(grouped.size());
                    out.add(row);
                });
    }

    private List<RmTrendPointEntity> buildTrendPoints(LocalDate from, LocalDate to, String departmentId, String teamId, List<FeedbackRecord> feedback) {
        List<RmTrendPointEntity> out = new ArrayList<>();
        appendTrendRows(out, from, to, departmentId, teamId, null, feedback);
        feedback.stream().collect(Collectors.groupingBy(FeedbackRecord::targetId))
                .forEach((personId, values) -> appendTrendRows(out, from, to, departmentId, teamId, personId, values));
        return out;
    }

    private void appendTrendRows(List<RmTrendPointEntity> out, LocalDate from, LocalDate to, String departmentId, String teamId, String personId, List<FeedbackRecord> values) {
        appendTrendRowsByGranularity(out, from, to, departmentId, teamId, personId, values, "month");
        appendTrendRowsByGranularity(out, from, to, departmentId, teamId, personId, values, "week");
    }

    private void appendTrendRowsByGranularity(List<RmTrendPointEntity> out, LocalDate from, LocalDate to, String departmentId, String teamId, String personId, List<FeedbackRecord> values, String granularity) {
        values.stream()
                .collect(Collectors.groupingBy(v -> bucket(v.date(), granularity), TreeMap::new, Collectors.toList()))
                .forEach((x, grouped) -> TREND_METRICS.forEach(metric -> {
                    RmTrendPointEntity row = new RmTrendPointEntity();
                    row.setPeriodFrom(from);
                    row.setPeriodTo(to);
                    row.setDepartmentId(departmentId);
                    row.setTeamId(teamId);
                    row.setPersonId(personId);
                    row.setMetric(metric);
                    row.setGranularity(granularity);
                    row.setX(x);
                    row.setY(metricValue(metric, grouped));
                    out.add(row);
                }));
    }

    private List<RmInsightsEntity> buildInsights(LocalDate from, LocalDate to, String departmentId, String teamId, List<FeedbackRecord> feedback) {
        List<InsightAggregate> aggregates = feedback.stream()
                .collect(Collectors.groupingBy(FeedbackRecord::subcategoryId))
                .values()
                .stream()
                .map(values -> new InsightAggregate(
                        values.getFirst().subcategoryId(),
                        values.getFirst().subcategoryName(),
                        round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)),
                        values.size()))
                .toList();

        List<RmInsightsEntity> out = new ArrayList<>();
        aggregates.stream().sorted(Comparator.comparingDouble(InsightAggregate::avgRating).reversed()).limit(5)
                .forEach(v -> out.add(toInsightsRow(from, to, departmentId, teamId, "best", v)));
        aggregates.stream().sorted(Comparator.comparingDouble(InsightAggregate::avgRating)).limit(5)
                .forEach(v -> out.add(toInsightsRow(from, to, departmentId, teamId, "worst", v)));
        return out;
    }

    private RmInsightsEntity toInsightsRow(LocalDate from, LocalDate to, String departmentId, String teamId, String type, InsightAggregate value) {
        RmInsightsEntity row = new RmInsightsEntity();
        row.setPeriodFrom(from);
        row.setPeriodTo(to);
        row.setDepartmentId(departmentId);
        row.setTeamId(teamId);
        row.setType(type);
        row.setSubcategoryId(value.subcategoryId());
        row.setName(value.name());
        row.setAvgRating(value.avgRating());
        row.setCount(value.count());
        return row;
    }

    private List<RmNetworkNodeEntity> buildNetworkNodes(LocalDate from, LocalDate to, String teamId, List<FeedbackRecord> feedback) {
        TreeMap<String, String> nodes = new TreeMap<>();
        feedback.forEach(v -> {
            nodes.putIfAbsent(v.authorId(), v.authorId());
            nodes.putIfAbsent(v.targetId(), v.targetName());
        });

        List<RmNetworkNodeEntity> out = new ArrayList<>();
        nodes.forEach((nodeId, label) -> {
            RmNetworkNodeEntity row = new RmNetworkNodeEntity();
            row.setPeriodFrom(from);
            row.setPeriodTo(to);
            row.setNodeId(nodeId);
            row.setLabel(label);
            row.setTeamId(teamId);
            out.add(row);
        });
        return out;
    }

    private List<RmNetworkEdgeEntity> buildNetworkEdges(LocalDate from, LocalDate to, String teamId, List<FeedbackRecord> feedback) {
        return feedback.stream()
                .collect(Collectors.groupingBy(v -> v.authorId() + "::" + v.targetId()))
                .values()
                .stream()
                .map(values -> {
                    FeedbackRecord sample = values.getFirst();
                    RmNetworkEdgeEntity row = new RmNetworkEdgeEntity();
                    row.setPeriodFrom(from);
                    row.setPeriodTo(to);
                    row.setTeamId(teamId);
                    row.setSource(sample.authorId());
                    row.setTarget(sample.targetId());
                    row.setWeight(values.size());
                    row.setAvgRating(round(values.stream().mapToInt(FeedbackRecord::rating).average().orElse(0.0)));
                    row.setPositive(values.stream().filter(v -> v.rating() >= 4).count());
                    row.setNegative(values.stream().filter(v -> v.rating() <= 2).count());
                    return row;
                })
                .toList();
    }

    private FeedbackRecord toFeedbackRecord(RmFeedbackEventEntity v) {
        return new FeedbackRecord(
                v.getFeedbackId(),
                v.getAuthorPersonId(),
                v.getTargetPersonId(),
                v.getTargetName(),
                v.getDepartmentId(),
                v.getTeamId(),
                v.getCategoryId(),
                v.getCategoryName(),
                v.getSubcategoryId(),
                v.getSubcategoryName(),
                v.getRating(),
                v.getFeedbackDate()
        );
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

    private String bucket(LocalDate date, String granularity) {
        if ("month".equals(granularity)) {
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        }
        return String.format("%d-W%02d", date.getYear(), date.get(WEEK_FIELDS.weekOfWeekBasedYear()));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record ProjectionScope(String departmentId, String teamId) {
    }

    private record InsightAggregate(String subcategoryId, String name, double avgRating, long count) {
    }
}
