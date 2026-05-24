package com.fitness.backend.service;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.ProgressReportRepository;
import com.fitness.backend.repository.UserRepository;
import com.fitness.backend.repository.WorkoutLogRepository;
import com.fitness.backend.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressTrackingService {

    private final WorkoutLogRepository workoutLogRepository;
    private final ProgressReportRepository progressReportRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    /**
     * Business Process: Progress Tracking
     * Inputs: Workout logs, Performance metrics
     * Method: User records sessions, System collects data, analyzes progress, generates reports
     * Output: Progress reports, Fitness statistics
     */
    @Transactional
    public WorkoutLog logWorkout(Long userId, WorkoutLogDTO logDTO) {
        log.info("Logging workout for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkoutPlan workoutPlan = null;
        if (logDTO.getWorkoutPlanId() != null) {
            workoutPlan = workoutPlanRepository.findById(logDTO.getWorkoutPlanId())
                    .orElse(null);
        }

        WorkoutLog workoutLog = WorkoutLog.builder()
                .user(user)
                .workoutPlan(workoutPlan)
                .workoutDate(logDTO.getWorkoutDate() != null ? logDTO.getWorkoutDate() : LocalDateTime.now())
                .durationMinutes(logDTO.getDurationMinutes())
                .setsCompleted(logDTO.getSetsCompleted())
                .repsCompleted(logDTO.getRepsCompleted())
                .weightUsed(logDTO.getWeightUsed())
                .distanceKm(logDTO.getDistanceKm())
                .caloriesBurned(logDTO.getCaloriesBurned())
                .heartRateAvg(logDTO.getHeartRateAvg())
                .notes(logDTO.getNotes())
                .difficultyRating(logDTO.getDifficultyRating())
                .build();

        workoutLog = workoutLogRepository.save(workoutLog);
        log.info("Workout logged successfully: {}", workoutLog.getId());

        return workoutLog;
    }

    /**
     * Get all workout logs for a user
     */
    @Transactional(readOnly = true)
    public List<WorkoutLog> getUserWorkoutLogs(Long userId) {
        return workoutLogRepository.findByUserIdOrderByWorkoutDateDesc(userId);
    }

    /**
     * Get workout logs for a specific period
     */
    @Transactional(readOnly = true)
    public List<WorkoutLog> getUserWorkoutLogsByPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return workoutLogRepository.findByUserIdAndWorkoutDateBetween(userId, startDate, endDate);
    }

    /**
     * Generate progress report for a user
     */
    @Transactional
    public ProgressReport generateProgressReport(Long userId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Generating progress report for user {} from {} to {}", userId, periodStart, periodEnd);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get workout logs for the period
        List<WorkoutLog> logs = workoutLogRepository.findByUserIdAndWorkoutDateBetween(
                userId, periodStart, periodEnd);

        if (logs.isEmpty()) {
            throw new RuntimeException("No workout data available for this period");
        }

        // Calculate metrics
        int totalWorkouts = logs.size();
        int totalDuration = logs.stream()
                .mapToInt(log -> log.getDurationMinutes() != null ? log.getDurationMinutes() : 0)
                .sum();
        int totalCalories = logs.stream()
                .mapToInt(log -> log.getCaloriesBurned() != null ? log.getCaloriesBurned() : 0)
                .sum();
        double avgDuration = totalWorkouts > 0 ? (double) totalDuration / totalWorkouts : 0;

        // Calculate completion rate (if they have an active plan)
        double completionRate = calculateCompletionRate(userId, periodStart, periodEnd);

        // Determine overall progress status
        ProgressReport.ProgressStatus status = determineProgressStatus(completionRate, totalWorkouts);

        // Generate summary and recommendations
        String summary = generateSummary(totalWorkouts, totalDuration, totalCalories);
        String recommendations = generateRecommendations(completionRate, totalWorkouts);

        ProgressReport report = ProgressReport.builder()
                .user(user)
                .reportDate(LocalDateTime.now())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .totalWorkouts(totalWorkouts)
                .totalDurationMinutes(totalDuration)
                .totalCaloriesBurned(totalCalories)
                .averageWorkoutDuration(avgDuration)
                .workoutCompletionRate(completionRate)
                .summary(summary)
                .recommendations(recommendations)
                .overallProgress(status)
                .build();

        report = progressReportRepository.save(report);
        log.info("Progress report generated: {}", report.getId());

        return report;
    }

    /**
     * Get all progress reports for a user
     */
    @Transactional(readOnly = true)
    public List<ProgressReport> getUserProgressReports(Long userId) {
        return progressReportRepository.findByUserIdOrderByReportDateDesc(userId);
    }

    /**
     * Get latest progress report
     */
    @Transactional(readOnly = true)
    public ProgressReport getLatestProgressReport(Long userId) {
        return progressReportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Calculate workout completion rate
     */
    private double calculateCompletionRate(Long userId, LocalDateTime start, LocalDateTime end) {
        List<WorkoutPlan> activePlans = workoutPlanRepository.findByUserIdAndIsActiveTrue(userId);
        if (activePlans.isEmpty()) return 100.0;

        WorkoutPlan plan = activePlans.get(0);
        int plannedWorkouts = plan.getExercises().size() *
                (int) java.time.temporal.ChronoUnit.WEEKS.between(start, end);

        if (plannedWorkouts == 0) return 100.0;

        int completedWorkouts = workoutLogRepository.findByUserIdAndWorkoutDateBetween(
                userId, start, end).size();

        return (double) completedWorkouts / plannedWorkouts * 100;
    }

    /**
     * Determine overall progress status
     */
    private ProgressReport.ProgressStatus determineProgressStatus(double completionRate, int totalWorkouts) {
        if (completionRate >= 90 && totalWorkouts >= 12) return ProgressReport.ProgressStatus.EXCELLENT;
        if (completionRate >= 75 && totalWorkouts >= 8) return ProgressReport.ProgressStatus.GOOD;
        if (completionRate >= 50 && totalWorkouts >= 4) return ProgressReport.ProgressStatus.AVERAGE;
        return ProgressReport.ProgressStatus.NEEDS_IMPROVEMENT;
    }

    /**
     * Generate summary text
     */
    private String generateSummary(int workouts, int duration, int calories) {
        return String.format(
                "You completed %d workouts with a total duration of %d minutes, burning %d calories. " +
                        "Average workout duration was %.1f minutes.",
                workouts, duration, calories, (double) duration / workouts
        );
    }

    /**
     * Generate recommendations
     */
    private String generateRecommendations(double completionRate, int workouts) {
        if (completionRate >= 90) {
            return "Excellent consistency! Consider increasing workout intensity or duration.";
        } else if (completionRate >= 75) {
            return "Good progress! Try to maintain this consistency and focus on proper form.";
        } else if (completionRate >= 50) {
            return "You're making progress. Try to increase workout frequency for better results.";
        } else {
            return "Consider setting more achievable goals and scheduling workouts at consistent times.";
        }
    }

    // DTO for workout logging
    public static class WorkoutLogDTO {
        private Long workoutPlanId;
        private Long exerciseId;
        private LocalDateTime workoutDate;
        private Integer durationMinutes;
        private Integer setsCompleted;
        private Integer repsCompleted;
        private Double weightUsed;
        private Double distanceKm;
        private Integer caloriesBurned;
        private Integer heartRateAvg;
        private String notes;
        private WorkoutLog.DifficultyRating difficultyRating;

        // Getters and setters
        public Long getWorkoutPlanId() { return workoutPlanId; }
        public void setWorkoutPlanId(Long workoutPlanId) { this.workoutPlanId = workoutPlanId; }
        public Long getExerciseId() { return exerciseId; }
        public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
        public LocalDateTime getWorkoutDate() { return workoutDate; }
        public void setWorkoutDate(LocalDateTime workoutDate) { this.workoutDate = workoutDate; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Integer getSetsCompleted() { return setsCompleted; }
        public void setSetsCompleted(Integer setsCompleted) { this.setsCompleted = setsCompleted; }
        public Integer getRepsCompleted() { return repsCompleted; }
        public void setRepsCompleted(Integer repsCompleted) { this.repsCompleted = repsCompleted; }
        public Double getWeightUsed() { return weightUsed; }
        public void setWeightUsed(Double weightUsed) { this.weightUsed = weightUsed; }
        public Double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
        public Integer getCaloriesBurned() { return caloriesBurned; }
        public void setCaloriesBurned(Integer caloriesBurned) { this.caloriesBurned = caloriesBurned; }
        public Integer getHeartRateAvg() { return heartRateAvg; }
        public void setHeartRateAvg(Integer heartRateAvg) { this.heartRateAvg = heartRateAvg; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public WorkoutLog.DifficultyRating getDifficultyRating() { return difficultyRating; }
        public void setDifficultyRating(WorkoutLog.DifficultyRating difficultyRating) {
            this.difficultyRating = difficultyRating;
        }
    }
}