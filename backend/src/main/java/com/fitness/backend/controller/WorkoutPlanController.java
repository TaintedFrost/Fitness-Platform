package com.fitness.backend.controller;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/workoutplans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkoutPlanController {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;

    // POST /api/workoutplans — called by coach dashboard to create a plan
    @PostMapping
    public ResponseEntity<?> createWorkoutPlan(@RequestBody Map<String, Object> body) {
        try {
            Long userId   = Long.valueOf(body.get("userId").toString());
            Long coachId  = Long.valueOf(body.get("coachId").toString());
            String name   = body.getOrDefault("name", "Untitled Plan").toString();

            User user  = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User coach = userRepository.findById(coachId)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));

            WorkoutPlan plan = new WorkoutPlan();
            plan.setName(name);
            plan.setUser(user);
            plan.setCoach(coach);
            plan.setIsActive(true);
            plan.setCreatedAt(LocalDateTime.now());
            plan.setUpdatedAt(LocalDateTime.now());

            if (body.containsKey("description"))
                plan.setDescription(body.get("description").toString());

            if (body.containsKey("durationWeeks") && body.get("durationWeeks") != null)
                plan.setDurationWeeks(Integer.valueOf(body.get("durationWeeks").toString()));

            if (body.containsKey("difficultyLevel") && body.get("difficultyLevel") != null) {
                try {
                    plan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.valueOf(
                            body.get("difficultyLevel").toString()));
                } catch (IllegalArgumentException ignored) {}
            }

            if (body.containsKey("startDate") && body.get("startDate") != null
                    && !body.get("startDate").toString().isBlank())
                plan.setStartDate(LocalDateTime.parse(body.get("startDate").toString() + "T00:00:00"));

            if (body.containsKey("endDate") && body.get("endDate") != null
                    && !body.get("endDate").toString().isBlank())
                plan.setEndDate(LocalDateTime.parse(body.get("endDate").toString() + "T00:00:00"));

            workoutPlanRepository.save(plan);

            return ResponseEntity.ok(Map.of(
                    "message", "Workout plan created successfully.",
                    "planId",  plan.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/workoutplans/user/{userId} — called by user dashboard
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPlansByUser(@PathVariable Long userId) {
        try {
            List<WorkoutPlan> plans = workoutPlanRepository.findByUserId(userId);
            List<Map<String, Object>> result = plans.stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id",              p.getId());
                m.put("name",            p.getName());
                m.put("description",     p.getDescription() != null ? p.getDescription() : "");
                m.put("difficultyLevel", p.getDifficultyLevel() != null ? p.getDifficultyLevel().name() : "");
                m.put("durationWeeks",   p.getDurationWeeks() != null ? p.getDurationWeeks() : 0);
                m.put("isActive",        p.getIsActive() != null ? p.getIsActive() : false);
                return m;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/workoutplans/{planId}
    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlan(@PathVariable Long planId) {
        return workoutPlanRepository.findById(planId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "id",          p.getId(),
                        "name",        p.getName(),
                        "description", p.getDescription() != null ? p.getDescription() : "",
                        "isActive",    p.getIsActive() != null ? p.getIsActive() : false
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/workoutplans/{planId}
    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        try {
            workoutPlanRepository.deleteById(planId);
            return ResponseEntity.ok(Map.of("message", "Plan deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}