package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_person_positivity")
@Getter
@Setter
public class RmPersonPositivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String departmentId;
    private String teamId;
    private String personId;
    private String displayName;
    private long positive;
    private long negative;
    private long total;
    private double avgRating;
}
