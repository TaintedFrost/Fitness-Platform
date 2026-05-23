package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String difficulty;

    private Long userId;
}