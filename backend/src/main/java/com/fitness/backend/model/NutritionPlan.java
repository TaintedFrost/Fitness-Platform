package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Coach who created it (nullable — can be auto-generated)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id")
    private User coach;

    private String title;

    // Aligned with user fitness goal
    @Column(name = "fitness_goal_target")
    private String fitnessGoalTarget;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Daily targets
    @Column(name = "daily_calories")
    private Integer dailyCalories;

    @Column(name = "protein_grams")
    private Integer proteinGrams;

    @Column(name = "carbs_grams")
    private Integer carbsGrams;

    @Column(name = "fat_grams")
    private Integer fatGrams;

    @Column(name = "water_liters")
    private Double waterLiters;

    // Meal recommendations
    @Column(name = "breakfast", columnDefinition = "TEXT")
    private String breakfast;

    @Column(name = "lunch", columnDefinition = "TEXT")
    private String lunch;

    @Column(name = "dinner", columnDefinition = "TEXT")
    private String dinner;

    @Column(name = "snacks", columnDefinition = "TEXT")
    private String snacks;

    @Column(columnDefinition = "TEXT")
    private String restrictions; // allergies, dietary restrictions

    @Column(columnDefinition = "TEXT")
    private String supplements;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}