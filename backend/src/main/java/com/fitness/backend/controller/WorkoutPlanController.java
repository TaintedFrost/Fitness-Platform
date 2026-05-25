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
    private final ExerciseRepository    exerciseRepository;
    private final UserRepository        userRepository;

    // ── CREATE plan ───────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createWorkoutPlan(@RequestBody Map<String, Object> body) {
        try {
            Long userId  = Long.valueOf(body.get("userId").toString());
            Long coachId = Long.valueOf(body.get("coachId").toString());
            String name  = body.getOrDefault("name", "Untitled Plan").toString();

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

            if (body.containsKey("description") && body.get("description") != null)
                plan.setDescription(body.get("description").toString());
            if (body.containsKey("durationWeeks") && body.get("durationWeeks") != null)
                plan.setDurationWeeks(Integer.valueOf(body.get("durationWeeks").toString()));
            if (body.containsKey("difficultyLevel") && body.get("difficultyLevel") != null) {
                try { plan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.valueOf(
                        body.get("difficultyLevel").toString())); } catch (Exception ignored) {}
            }
            if (body.containsKey("startDate") && body.get("startDate") != null
                    && !body.get("startDate").toString().isBlank())
                plan.setStartDate(LocalDateTime.parse(body.get("startDate") + "T00:00:00"));
            if (body.containsKey("endDate") && body.get("endDate") != null
                    && !body.get("endDate").toString().isBlank())
                plan.setEndDate(LocalDateTime.parse(body.get("endDate") + "T00:00:00"));

            workoutPlanRepository.save(plan);
            return ResponseEntity.ok(Map.of("message", "Plan created!", "planId", plan.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET plans by user ─────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPlansByUser(@PathVariable Long userId) {
        try {
            List<WorkoutPlan> plans = workoutPlanRepository.findByUserId(userId);
            return ResponseEntity.ok(plans.stream().map(p -> toPlanMap(p, false)).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET plans by coach ────────────────────────────────
    @GetMapping("/coach/{coachId}")
    public ResponseEntity<?> getPlansByCoach(@PathVariable Long coachId) {
        try {
            List<WorkoutPlan> plans = workoutPlanRepository.findAll().stream()
                    .filter(p -> p.getCoach() != null && p.getCoach().getId().equals(coachId))
                    .toList();
            return ResponseEntity.ok(plans.stream().map(p -> toPlanMap(p, true)).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET single plan with exercises ────────────────────
    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlan(@PathVariable Long planId) {
        return workoutPlanRepository.findById(planId)
                .map(p -> ResponseEntity.ok(toPlanMap(p, true)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ADD exercise to plan ──────────────────────────────
    @PostMapping("/{planId}/exercises")
    public ResponseEntity<?> addExercise(
            @PathVariable Long planId,
            @RequestBody Map<String, Object> body) {
        try {
            WorkoutPlan plan = workoutPlanRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            Exercise ex = new Exercise();
            ex.setWorkoutPlan(plan);
            ex.setName(body.getOrDefault("name", "Exercise").toString());

            if (body.containsKey("description") && body.get("description") != null)
                ex.setDescription(body.get("description").toString());
            if (body.containsKey("instructions") && body.get("instructions") != null)
                ex.setInstructions(body.get("instructions").toString());
            if (body.containsKey("sets") && body.get("sets") != null)
                ex.setSets(Integer.valueOf(body.get("sets").toString()));
            if (body.containsKey("reps") && body.get("reps") != null)
                ex.setReps(Integer.valueOf(body.get("reps").toString()));
            if (body.containsKey("durationMinutes") && body.get("durationMinutes") != null)
                ex.setDurationMinutes(Integer.valueOf(body.get("durationMinutes").toString()));
            if (body.containsKey("restSeconds") && body.get("restSeconds") != null)
                ex.setRestSeconds(Integer.valueOf(body.get("restSeconds").toString()));
            if (body.containsKey("dayOfWeek") && body.get("dayOfWeek") != null)
                ex.setDayOfWeek(Integer.valueOf(body.get("dayOfWeek").toString()));
            if (body.containsKey("orderIndex") && body.get("orderIndex") != null)
                ex.setOrderIndex(Integer.valueOf(body.get("orderIndex").toString()));
            if (body.containsKey("muscleGroup") && body.get("muscleGroup") != null)
                ex.setMuscleGroup(body.get("muscleGroup").toString());
            if (body.containsKey("videoUrl") && body.get("videoUrl") != null)
                ex.setVideoUrl(body.get("videoUrl").toString());
            if (body.containsKey("exerciseType") && body.get("exerciseType") != null) {
                try { ex.setExerciseType(Exercise.ExerciseType.valueOf(
                        body.get("exerciseType").toString())); } catch (Exception ignored) {}
            }

            exerciseRepository.save(ex);
            return ResponseEntity.ok(toExerciseMap(ex));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE exercise ───────────────────────────────────
    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long exerciseId) {
        try {
            exerciseRepository.deleteById(exerciseId);
            return ResponseEntity.ok(Map.of("message", "Exercise deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE plan ───────────────────────────────────────
    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        try {
            workoutPlanRepository.deleteById(planId);
            return ResponseEntity.ok(Map.of("message", "Plan deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Safe serialization ────────────────────────────────
    private Map<String, Object> toPlanMap(WorkoutPlan p, boolean includeExercises) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",              p.getId());
        m.put("name",            p.getName());
        m.put("description",     p.getDescription() != null ? p.getDescription() : "");
        m.put("difficultyLevel", p.getDifficultyLevel() != null ? p.getDifficultyLevel().name() : "");
        m.put("durationWeeks",   p.getDurationWeeks()  != null ? p.getDurationWeeks()  : 0);
        m.put("isActive",        p.getIsActive()       != null ? p.getIsActive()       : false);
        m.put("startDate",       p.getStartDate()      != null ? p.getStartDate().toString() : "");
        m.put("endDate",         p.getEndDate()        != null ? p.getEndDate().toString()   : "");
        if (p.getUser() != null)
            m.put("userName", p.getUser().getFullName() != null ? p.getUser().getFullName() : "");
        if (p.getCoach() != null)
            m.put("coachName", p.getCoach().getFullName() != null ? p.getCoach().getFullName() : "");
        if (includeExercises)
            m.put("exercises", p.getExercises().stream().map(this::toExerciseMap).toList());
        return m;
    }

    private Map<String, Object> toExerciseMap(Exercise e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",              e.getId());
        m.put("name",            e.getName());
        m.put("description",     e.getDescription()    != null ? e.getDescription()    : "");
        m.put("instructions",    e.getInstructions()   != null ? e.getInstructions()   : "");
        m.put("sets",            e.getSets()           != null ? e.getSets()           : 0);
        m.put("reps",            e.getReps()           != null ? e.getReps()           : 0);
        m.put("durationMinutes", e.getDurationMinutes()!= null ? e.getDurationMinutes(): 0);
        m.put("restSeconds",     e.getRestSeconds()    != null ? e.getRestSeconds()    : 0);
        m.put("dayOfWeek",       e.getDayOfWeek()      != null ? e.getDayOfWeek()      : 0);
        m.put("orderIndex",      e.getOrderIndex()     != null ? e.getOrderIndex()     : 0);
        m.put("muscleGroup",     e.getMuscleGroup()    != null ? e.getMuscleGroup()    : "");
        m.put("videoUrl",        e.getVideoUrl()       != null ? e.getVideoUrl()       : "");
        m.put("exerciseType",    e.getExerciseType()   != null ? e.getExerciseType().name() : "");
        return m;
    }
}