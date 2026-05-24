package com.fitness.backend.controller;

import com.fitness.backend.model.ProgressReport;
import com.fitness.backend.model.WorkoutLog;
import com.fitness.backend.service.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressTrackingService progressTrackingService;

    // POST /api/progress/log-workout
    @PostMapping("/log-workout")
    public ResponseEntity<?> logWorkout(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            ProgressTrackingService.WorkoutLogDTO dto = new ProgressTrackingService.WorkoutLogDTO();

            if (body.containsKey("workoutPlanId") && body.get("workoutPlanId") != null)
                dto.setWorkoutPlanId(Long.valueOf(body.get("workoutPlanId").toString()));
            if (body.containsKey("workoutDate") && body.get("workoutDate") != null)
                dto.setWorkoutDate(LocalDateTime.parse(body.get("workoutDate").toString()));
            if (body.containsKey("durationMinutes") && body.get("durationMinutes") != null)
                dto.setDurationMinutes(Integer.valueOf(body.get("durationMinutes").toString()));
            if (body.containsKey("caloriesBurned") && body.get("caloriesBurned") != null)
                dto.setCaloriesBurned(Integer.valueOf(body.get("caloriesBurned").toString()));
            if (body.containsKey("setsCompleted") && body.get("setsCompleted") != null)
                dto.setSetsCompleted(Integer.valueOf(body.get("setsCompleted").toString()));
            if (body.containsKey("repsCompleted") && body.get("repsCompleted") != null)
                dto.setRepsCompleted(Integer.valueOf(body.get("repsCompleted").toString()));
            if (body.containsKey("weightUsed") && body.get("weightUsed") != null)
                dto.setWeightUsed(Double.valueOf(body.get("weightUsed").toString()));
            if (body.containsKey("distanceKm") && body.get("distanceKm") != null)
                dto.setDistanceKm(Double.valueOf(body.get("distanceKm").toString()));
            if (body.containsKey("heartRateAvg") && body.get("heartRateAvg") != null)
                dto.setHeartRateAvg(Integer.valueOf(body.get("heartRateAvg").toString()));
            if (body.containsKey("notes") && body.get("notes") != null)
                dto.setNotes(body.get("notes").toString());
            if (body.containsKey("difficultyRating") && body.get("difficultyRating") != null) {
                try {
                    dto.setDifficultyRating(WorkoutLog.DifficultyRating.valueOf(
                            body.get("difficultyRating").toString()));
                } catch (IllegalArgumentException ignored) {}
            }

            WorkoutLog log = progressTrackingService.logWorkout(userId, dto);
            return ResponseEntity.ok(Map.of("message", "Workout logged!", "id", log.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/progress/workout-logs?userId=1
    @GetMapping("/workout-logs")
    public ResponseEntity<?> getWorkoutLogs(@RequestParam Long userId) {
        try {
            List<WorkoutLog> logs = progressTrackingService.getUserWorkoutLogs(userId);
            List<Map<String, Object>> result = logs.stream().map(this::toLogMap).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/progress/generate-report
    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody Map<String, Object> body) {
        try {
            Long userId      = Long.valueOf(body.get("userId").toString());
            LocalDateTime start = LocalDateTime.parse(body.get("periodStart").toString());
            LocalDateTime end   = LocalDateTime.parse(body.get("periodEnd").toString());

            ProgressReport report = progressTrackingService.generateProgressReport(userId, start, end);
            return ResponseEntity.ok(toReportMap(report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/progress/reports?userId=1
    @GetMapping("/reports")
    public ResponseEntity<?> getProgressReports(@RequestParam Long userId) {
        try {
            List<ProgressReport> reports = progressTrackingService.getUserProgressReports(userId);
            return ResponseEntity.ok(reports.stream().map(this::toReportMap).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/progress/reports/latest?userId=1
    @GetMapping("/reports/latest")
    public ResponseEntity<?> getLatestReport(@RequestParam Long userId) {
        try {
            ProgressReport report = progressTrackingService.getLatestProgressReport(userId);
            if (report == null)
                return ResponseEntity.ok(Map.of("message", "No reports yet"));
            return ResponseEntity.ok(toReportMap(report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Safe serialization helpers ────────────────────────
    private Map<String, Object> toLogMap(WorkoutLog l) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",              l.getId());
        m.put("workoutDate",     l.getWorkoutDate() != null ? l.getWorkoutDate().toString() : "");
        m.put("durationMinutes", l.getDurationMinutes() != null ? l.getDurationMinutes() : 0);
        m.put("caloriesBurned",  l.getCaloriesBurned() != null ? l.getCaloriesBurned() : 0);
        m.put("setsCompleted",   l.getSetsCompleted() != null ? l.getSetsCompleted() : 0);
        m.put("repsCompleted",   l.getRepsCompleted() != null ? l.getRepsCompleted() : 0);
        m.put("weightUsed",      l.getWeightUsed() != null ? l.getWeightUsed() : 0);
        m.put("distanceKm",      l.getDistanceKm() != null ? l.getDistanceKm() : 0);
        m.put("heartRateAvg",    l.getHeartRateAvg() != null ? l.getHeartRateAvg() : 0);
        m.put("notes",           l.getNotes() != null ? l.getNotes() : "");
        m.put("difficultyRating",l.getDifficultyRating() != null ? l.getDifficultyRating().name() : "");
        if (l.getWorkoutPlan() != null)
            m.put("workoutPlanName", l.getWorkoutPlan().getName());
        return m;
    }

    private Map<String, Object> toReportMap(ProgressReport r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",                    r.getId());
        m.put("reportDate",            r.getReportDate() != null ? r.getReportDate().toString() : "");
        m.put("periodStart",           r.getPeriodStart() != null ? r.getPeriodStart().toString() : "");
        m.put("periodEnd",             r.getPeriodEnd() != null ? r.getPeriodEnd().toString() : "");
        m.put("totalWorkouts",         r.getTotalWorkouts() != null ? r.getTotalWorkouts() : 0);
        m.put("totalDurationMinutes",  r.getTotalDurationMinutes() != null ? r.getTotalDurationMinutes() : 0);
        m.put("totalCaloriesBurned",   r.getTotalCaloriesBurned() != null ? r.getTotalCaloriesBurned() : 0);
        m.put("averageWorkoutDuration",r.getAverageWorkoutDuration() != null ? r.getAverageWorkoutDuration() : 0);
        m.put("workoutCompletionRate", r.getWorkoutCompletionRate() != null ? r.getWorkoutCompletionRate() : 0);
        m.put("summary",               r.getSummary() != null ? r.getSummary() : "");
        m.put("recommendations",       r.getRecommendations() != null ? r.getRecommendations() : "");
        m.put("overallProgress",       r.getOverallProgress() != null ? r.getOverallProgress().name() : "");
        return m;
    }
}