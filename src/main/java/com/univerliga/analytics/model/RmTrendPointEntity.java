package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_trend_points")
@Getter
@Setter
public class RmTrendPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String metric;
    private String granularity;
    @Column(name = "x_value")
    private String x;
    @Column(name = "y_value")
    private double y;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String departmentId;
    private String teamId;
    private String personId;
}
