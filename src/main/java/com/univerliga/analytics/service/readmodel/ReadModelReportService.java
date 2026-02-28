package com.univerliga.analytics.service.readmodel;

import com.univerliga.analytics.dto.*;
import com.univerliga.analytics.model.*;
import com.univerliga.analytics.repository.*;
import com.univerliga.analytics.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ReadModelReportService implements ReportService {

    private final RmKpiSummaryRepository kpiSummaryRepository;
    private final RmPersonPositivityRepository personPositivityRepository;
    private final RmSubcategoryFreqRepository subcategoryFreqRepository;
    private final RmCategoryRatingsRepository categoryRatingsRepository;
    private final RmTrendPointRepository trendPointRepository;
    private final RmInsightsRepository insightsRepository;
    private final RmNetworkNodeRepository networkNodeRepository;
    private final RmNetworkEdgeRepository networkEdgeRepository;

    public ReadModelReportService(
            RmKpiSummaryRepository kpiSummaryRepository,
            RmPersonPositivityRepository personPositivityRepository,
            RmSubcategoryFreqRepository subcategoryFreqRepository,
            RmCategoryRatingsRepository categoryRatingsRepository,
            RmTrendPointRepository trendPointRepository,
            RmInsightsRepository insightsRepository,
            RmNetworkNodeRepository networkNodeRepository,
            RmNetworkEdgeRepository networkEdgeRepository) {
        this.kpiSummaryRepository = kpiSummaryRepository;
        this.personPositivityRepository = personPositivityRepository;
        this.subcategoryFreqRepository = subcategoryFreqRepository;
        this.categoryRatingsRepository = categoryRatingsRepository;
        this.trendPointRepository = trendPointRepository;
        this.insightsRepository = insightsRepository;
        this.networkNodeRepository = networkNodeRepository;
        this.networkEdgeRepository = networkEdgeRepository;
    }

    @Override
    public SummaryResponse summary(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        RmKpiSummaryEntity item = kpiSummaryRepository
                .findFirstByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(from, to, normalize(departmentId), normalize(teamId), normalize(personId))
                .orElseGet(RmKpiSummaryEntity::new);
        return new SummaryResponse(
                period(from, to),
                scope(departmentId, teamId, personId, null),
                new SummaryResponse.Kpis(item.getResponses(), item.getUniqueAuthors(), item.getUniqueTargets(), item.getAvgRating(), item.getPositiveShare(), item.getNegativeShare())
        );
    }

    @Override
    public PositivityByPersonResponse positivityByPerson(LocalDate from, LocalDate to, String departmentId, String teamId, int limit, String sort) {
        Comparator<PositivityByPersonResponse.Item> comparator = switch (sort == null ? "total" : sort) {
            case "positive" -> Comparator.comparingLong(PositivityByPersonResponse.Item::positive).reversed();
            case "negative" -> Comparator.comparingLong(PositivityByPersonResponse.Item::negative).reversed();
            case "avgRating" -> Comparator.comparingDouble(PositivityByPersonResponse.Item::avgRating).reversed();
            default -> Comparator.comparingLong(PositivityByPersonResponse.Item::total).reversed();
        };

        List<PositivityByPersonResponse.Item> items = personPositivityRepository
                .findByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, normalize(departmentId), normalize(teamId)).stream()
                .map(v -> new PositivityByPersonResponse.Item(v.getPersonId(), v.getDisplayName(), v.getPositive(), v.getNegative(), v.getTotal(), v.getAvgRating()))
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

        List<SubcategoryFrequencyResponse.Item> items = subcategoryFreqRepository
                .findByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonIdAndCategoryId(from, to, normalize(departmentId), normalize(teamId), normalize(personId), normalize(categoryId)).stream()
                .map(v -> new SubcategoryFrequencyResponse.Item(v.getSubcategoryId(), v.getSubcategoryName(), v.getPositive(), v.getNegative(), v.getTotal()))
                .sorted(comparator)
                .limit(limit)
                .toList();

        return new SubcategoryFrequencyResponse(period(from, to), scope(departmentId, teamId, personId, categoryId), items);
    }

    @Override
    public TrendResponse trend(String metric, String granularity, LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        List<TrendResponse.Point> points = trendPointRepository
                .findByMetricAndGranularityAndPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(
                        metric, granularity, from, to, normalize(departmentId), normalize(teamId), normalize(personId)).stream()
                .sorted(Comparator.comparing(RmTrendPointEntity::getX))
                .map(v -> new TrendResponse.Point(v.getX(), v.getY()))
                .toList();

        return new TrendResponse(metric, granularity, period(from, to), points);
    }

    @Override
    public RatingsByCategoryResponse ratingsByCategory(LocalDate from, LocalDate to, String departmentId, String teamId, String personId) {
        List<RatingsByCategoryResponse.Item> series = categoryRatingsRepository
                .findByPeriodFromAndPeriodToAndDepartmentIdAndTeamIdAndPersonId(from, to, normalize(departmentId), normalize(teamId), normalize(personId)).stream()
                .map(v -> new RatingsByCategoryResponse.Item(v.getCategoryId(), v.getCategoryName(), v.getAvgRating(), v.getCount()))
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
        List<RmInsightsEntity> items = insightsRepository.findByPeriodFromAndPeriodToAndDepartmentIdAndTeamId(from, to, normalize(departmentId), normalize(teamId));
        List<InsightsResponse.Item> best = items.stream().filter(v -> "best".equals(v.getType())).limit(limit)
                .map(v -> new InsightsResponse.Item(v.getSubcategoryId(), v.getName(), v.getAvgRating(), v.getCount())).toList();
        List<InsightsResponse.Item> worst = items.stream().filter(v -> "worst".equals(v.getType())).limit(limit)
                .map(v -> new InsightsResponse.Item(v.getSubcategoryId(), v.getName(), v.getAvgRating(), v.getCount())).toList();
        return new InsightsResponse(period(from, to), best, worst);
    }

    @Override
    public NetworkResponse network(LocalDate from, LocalDate to, String teamId) {
        List<NetworkResponse.Node> nodes = networkNodeRepository.findByPeriodFromAndPeriodToAndTeamId(from, to, normalize(teamId)).stream()
                .map(v -> new NetworkResponse.Node(v.getNodeId(), v.getLabel(), v.getTeamId())).toList();
        List<NetworkResponse.Edge> edges = networkEdgeRepository.findByPeriodFromAndPeriodToAndTeamId(from, to, normalize(teamId)).stream()
                .map(v -> new NetworkResponse.Edge(v.getSource(), v.getTarget(), v.getWeight(), v.getAvgRating(), v.getPositive(), v.getNegative())).toList();
        return new NetworkResponse(period(from, to), nodes, edges);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private PeriodDto period(LocalDate from, LocalDate to) {
        return new PeriodDto(from.toString(), to.toString());
    }

    private ScopeDto scope(String departmentId, String teamId, String personId, String categoryId) {
        return new ScopeDto(normalize(departmentId), normalize(teamId), normalize(personId), normalize(categoryId));
    }
}
