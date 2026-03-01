package com.univerliga.analytics.ingestion;

import com.univerliga.analytics.config.BrokerIngestionProperties;
import com.univerliga.analytics.model.IngestionProcessedEventEntity;
import com.univerliga.analytics.model.RmFeedbackEventEntity;
import com.univerliga.analytics.repository.IngestionProcessedEventRepository;
import com.univerliga.analytics.repository.RmFeedbackEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedbackEventIngestionService {

    private final IngestionProcessedEventRepository processedEventRepository;
    private final RmFeedbackEventRepository feedbackEventRepository;
    private final IncrementalReadModelProjector readModelProjector;
    private final BrokerIngestionProperties ingestionProperties;

    @Transactional
    public IngestionResult process(FeedbackIngestionEvent event, String routingKey) {
        if (event == null || event.eventId() == null || event.payload() == null || event.type() == null) {
            throw new IllegalArgumentException("Invalid event envelope");
        }

        if (processedEventRepository.existsById(event.eventId())) {
            return IngestionResult.duplicate(event.eventId());
        }

        FeedbackEventPayload payload = event.payload();
        if (isBlank(payload.feedbackId())) {
            throw new IllegalArgumentException("payload.feedbackId is required");
        }
        if (isBlank(payload.departmentId()) || isBlank(payload.teamId())) {
            throw new IllegalArgumentException("payload.departmentId and payload.teamId are required");
        }

        Optional<RmFeedbackEventEntity> previous = feedbackEventRepository.findById(payload.feedbackId());

        if (event.type().isDelete()) {
            feedbackEventRepository.deleteById(payload.feedbackId());
        } else {
            if (isBlank(payload.authorPersonId())
                    || isBlank(payload.targetPersonId())
                    || payload.feedbackDate() == null
                    || isBlank(payload.categoryId())
                    || isBlank(payload.categoryName())
                    || isBlank(payload.subcategoryId())
                    || isBlank(payload.subcategoryName())) {
                throw new IllegalArgumentException("payload fields are incomplete for upsert event");
            }
            feedbackEventRepository.save(toFact(event, payload));
        }

        Set<IncrementalReadModelProjector.ProjectionScope> scopes = new HashSet<>();
        previous.ifPresent(v -> scopes.add(new IncrementalReadModelProjector.ProjectionScope(v.getDepartmentId(), v.getTeamId())));
        scopes.add(new IncrementalReadModelProjector.ProjectionScope(payload.departmentId(), payload.teamId()));

        readModelProjector.reproject(
                ingestionProperties.getProjection().getPeriodFrom(),
                ingestionProperties.getProjection().getPeriodTo(),
                scopes
        );

        try {
            processedEventRepository.save(toProcessed(event, routingKey));
        } catch (DataIntegrityViolationException ignored) {
            return IngestionResult.duplicate(event.eventId());
        }
        return IngestionResult.processed(event.eventId());
    }

    private IngestionProcessedEventEntity toProcessed(FeedbackIngestionEvent event, String routingKey) {
        IngestionProcessedEventEntity row = new IngestionProcessedEventEntity();
        row.setEventId(event.eventId());
        row.setOccurredAt(event.occurredAt());
        row.setSource(event.source());
        row.setRoutingKey(routingKey);
        return row;
    }

    private RmFeedbackEventEntity toFact(FeedbackIngestionEvent event, FeedbackEventPayload payload) {
        RmFeedbackEventEntity row = new RmFeedbackEventEntity();
        row.setFeedbackId(payload.feedbackId());
        row.setEventId(event.eventId());
        row.setEventType(event.type().name());
        row.setEventOccurredAt(event.occurredAt() == null ? OffsetDateTime.now() : event.occurredAt());
        row.setAuthorPersonId(payload.authorPersonId());
        row.setTargetPersonId(payload.targetPersonId());
        row.setTargetName(payload.targetName() == null ? payload.targetPersonId() : payload.targetName());
        row.setDepartmentId(payload.departmentId());
        row.setTeamId(payload.teamId());
        row.setCategoryId(payload.categoryId());
        row.setCategoryName(payload.categoryName());
        row.setSubcategoryId(payload.subcategoryId());
        row.setSubcategoryName(payload.subcategoryName());
        row.setRating(payload.rating() == null ? 3 : payload.rating());
        row.setFeedbackDate(payload.feedbackDate());
        return row;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record IngestionResult(String eventId, boolean duplicate) {
        static IngestionResult duplicate(String eventId) {
            return new IngestionResult(eventId, true);
        }

        static IngestionResult processed(String eventId) {
            return new IngestionResult(eventId, false);
        }
    }
}
