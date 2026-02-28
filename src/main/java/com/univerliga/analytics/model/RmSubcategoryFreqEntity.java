package com.univerliga.analytics.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rm_subcategory_freq")
@Getter
@Setter
public class RmSubcategoryFreqEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private String departmentId;
    private String teamId;
    private String personId;
    private String categoryId;
    private String subcategoryId;
    private String subcategoryName;
    private long positive;
    private long negative;
    private long total;
}
