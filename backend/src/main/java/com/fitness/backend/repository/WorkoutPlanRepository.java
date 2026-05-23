package com.fitness.backend.repository;

import com.fitness.backend.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutPlanRepository
        extends JpaRepository<WorkoutPlan, Long> {
}