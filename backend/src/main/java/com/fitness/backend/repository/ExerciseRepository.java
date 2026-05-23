package com.fitness.backend.repository;

import com.fitness.backend.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository
        extends JpaRepository<Exercise, Long> {
}