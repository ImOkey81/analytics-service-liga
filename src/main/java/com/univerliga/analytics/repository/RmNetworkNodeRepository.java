package com.univerliga.analytics.repository;

import com.univerliga.analytics.model.RmNetworkNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RmNetworkNodeRepository extends JpaRepository<RmNetworkNodeEntity, Long> {
    List<RmNetworkNodeEntity> findByPeriodFromAndPeriodToAndTeamId(LocalDate periodFrom, LocalDate periodTo, String teamId);

    void deleteByPeriodFromAndPeriodToAndTeamId(LocalDate periodFrom, LocalDate periodTo, String teamId);
}
