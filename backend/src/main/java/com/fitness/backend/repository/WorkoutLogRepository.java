package com.fitness.backend.repository;

import com.fitness.backend.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    List<WorkoutLog> findByUserId(Long userId);

    List<WorkoutLog> findByUserIdOrderByWorkoutDateDesc(Long userId);

    List<WorkoutLog> findByUserIdAndWorkoutDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<WorkoutLog> findByWorkoutPlanId(Long workoutPlanId);

    long countByUserId(Long userId);
}