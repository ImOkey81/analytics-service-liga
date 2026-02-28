package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_insights")
@Getter
@Setter
public class RmInsightsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String departmentId;
    private String teamId;
    private String type;
    private String subcategoryId;
    private String name;
    private double avgRating;
    private long count;
}
