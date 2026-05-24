package com.fitness.backend.repository;

import com.fitness.backend.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    List<WorkoutLog> findByUserIdOrderByWorkoutDateDesc(Long userId);
    List<WorkoutLog> findByUserIdAndWorkoutDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
}