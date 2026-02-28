package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_network_nodes")
@Getter
@Setter
public class RmNetworkNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String nodeId;
    private String label;
    private String teamId;
}
