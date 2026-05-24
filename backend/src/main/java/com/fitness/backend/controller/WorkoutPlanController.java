package com.fitness.backend.controller;

import com.fitness.backend.model.Exercise;
import com.fitness.backend.model.User;
import com.fitness.backend.model.WorkoutPlan;
import com.fitness.backend.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    /**
     * Create a new workout plan (Coach only)
     */
    @PostMapping("/create/{clientId}")
    public ResponseEntity<?> createWorkoutPlan(
            @PathVariable Long clientId,
            @RequestBody WorkoutPlanService.WorkoutPlanDTO planDTO,
            Authentication authentication) {
        try {
            Long coachId = getUserIdFromAuth(authentication);
            WorkoutPlan plan = workoutPlanService.createWorkoutPlan(coachId, clientId, planDTO);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Add exercise to workout plan
     */
    @PostMapping("/{planId}/exercises")
    public ResponseEntity<?> addExercise(
            @PathVariable Long planId,
            @RequestBody WorkoutPlanService.ExerciseDTO exerciseDTO) {
        try {
            Exercise exercise = workoutPlanService.addExerciseToWorkoutPlan(planId, exerciseDTO);
            return ResponseEntity.ok(exercise);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get active workout plan for current user
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveWorkoutPlan(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            WorkoutPlan plan = workoutPlanService.getActiveWorkoutPlan(userId);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get all workout plans for current user
     */
    @GetMapping("/my-plans")
    public ResponseEntity<?> getMyWorkoutPlans(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<WorkoutPlan> plans = workoutPlanService.getAllUserWorkoutPlans(userId);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Update workout plan
     */
    @PutMapping("/{planId}")
    public ResponseEntity<?> updateWorkoutPlan(
            @PathVariable Long planId,
            @RequestBody WorkoutPlanService.WorkoutPlanDTO planDTO) {
        try {
            WorkoutPlan plan = workoutPlanService.updateWorkoutPlan(planId, planDTO);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Delete exercise
     */
    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long exerciseId) {
        try {
            workoutPlanService.deleteExercise(exerciseId);
            return ResponseEntity.ok(Map.of("message", "Exercise deleted successfully"));
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