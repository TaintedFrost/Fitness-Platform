package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercises")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private Integer sets;

    private Integer reps;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1-7, Monday-Sunday

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "muscle_group")
    private String muscleGroup; // e.g., "Chest", "Legs", "Back"

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type")
    private ExerciseType exerciseType;

    public enum ExerciseType {
        STRENGTH, CARDIO, FLEXIBILITY, BALANCE, SPORTS
    }
}