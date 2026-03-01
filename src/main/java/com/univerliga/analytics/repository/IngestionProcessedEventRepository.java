package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.IngestionProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionProcessedEventRepository extends JpaRepository<IngestionProcessedEventEntity, String> {
}
