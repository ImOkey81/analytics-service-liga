package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_network_edges")
@Getter
@Setter
public class RmNetworkEdgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String source;
    private String target;
    private long weight;
    private double avgRating;
    private long positive;
    private long negative;
    private String teamId;
}
