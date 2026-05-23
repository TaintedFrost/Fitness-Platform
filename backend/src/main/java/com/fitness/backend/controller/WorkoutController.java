package com.fitness.backend.controller;

import com.fitness.backend.model.WorkoutPlan;
import com.fitness.backend.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@CrossOrigin
public class WorkoutController {

    private final WorkoutPlanRepository workoutPlanRepository;

    @PostMapping
    public WorkoutPlan createWorkout(
            @RequestBody WorkoutPlan workoutPlan
    ) {

        return workoutPlanRepository.save(workoutPlan);
    }

    @GetMapping
    public List<WorkoutPlan> getAllWorkouts() {

        return workoutPlanRepository.findAll();
    }

    @GetMapping("/{id}")
    public WorkoutPlan getWorkoutById(
            @PathVariable Long id
    ) {

        return workoutPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Workout not found"));
    }

    @DeleteMapping("/{id}")
    public String deleteWorkout(
            @PathVariable Long id
    ) {

        workoutPlanRepository.deleteById(id);

        return "Workout deleted";
    }
}