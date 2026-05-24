package com.fitness.backend.controller;

import com.fitness.backend.model.ProgressReport;
import com.fitness.backend.model.User;
import com.fitness.backend.model.WorkoutLog;
import com.fitness.backend.service.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressTrackingService progressTrackingService;

    /**
     * Log a workout
     */
    @PostMapping("/log-workout")
    public ResponseEntity<?> logWorkout(
            @RequestBody ProgressTrackingService.WorkoutLogDTO logDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            WorkoutLog log = progressTrackingService.logWorkout(userId, logDTO);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get all workout logs for current user
     */
    @GetMapping("/workout-logs")
    public ResponseEntity<?> getWorkoutLogs(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<WorkoutLog> logs = progressTrackingService.getUserWorkoutLogs(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get workout logs for a specific period
     */
    @GetMapping("/workout-logs/period")
    public ResponseEntity<?> getWorkoutLogsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<WorkoutLog> logs = progressTrackingService.getUserWorkoutLogsByPeriod(
                    userId, startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Generate a progress report
     */
    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            ProgressReport report = progressTrackingService.generateProgressReport(
                    userId, periodStart, periodEnd);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get all progress reports for current user
     */
    @GetMapping("/reports")
    public ResponseEntity<?> getProgressReports(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<ProgressReport> reports = progressTrackingService.getUserProgressReports(userId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get latest progress report
     */
    @GetMapping("/reports/latest")
    public ResponseEntity<?> getLatestReport(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            ProgressReport report = progressTrackingService.getLatestProgressReport(userId);
            if (report == null) {
                return ResponseEntity.ok(Map.of("message", "No reports available"));
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}