package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_category_ratings")
@Getter
@Setter
public class RmCategoryRatingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String departmentId;
    private String teamId;
    private String personId;
    private String categoryId;
    private String categoryName;
    private double avgRating;
    private long count;
}
