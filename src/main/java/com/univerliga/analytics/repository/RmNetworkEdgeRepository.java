package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmNetworkEdgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmNetworkEdgeRepository extends JpaRepository<RmNetworkEdgeEntity, Long> {
    List<RmNetworkEdgeEntity> findByPeriodFromAndPeriodToAndTeamId(LocalDate periodFrom, LocalDate periodTo, String teamId);
}
