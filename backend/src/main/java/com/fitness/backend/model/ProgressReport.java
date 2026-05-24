package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    // Aggregated metrics
    @Column(name = "total_workouts")
    private Integer totalWorkouts;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @Column(name = "total_calories_burned")
    private Integer totalCaloriesBurned;

    @Column(name = "average_workout_duration")
    private Double averageWorkoutDuration;

    @Column(name = "workout_completion_rate")
    private Double workoutCompletionRate; // percentage

    // Progress metrics
    @Column(name = "weight_change_kg")
    private Double weightChangeKg;

    @Column(name = "strength_improvement_percent")
    private Double strengthImprovementPercent;

    @Column(name = "endurance_improvement_percent")
    private Double enduranceImprovementPercent;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_progress")
    private ProgressStatus overallProgress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ProgressStatus {
        EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT
    }
}