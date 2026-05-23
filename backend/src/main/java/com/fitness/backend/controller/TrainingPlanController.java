package com.fitness.backend.controller;

import com.fitness.backend.model.TrainingPlan;
import com.fitness.backend.repository.TrainingPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@CrossOrigin
public class TrainingPlanController {

    private final TrainingPlanRepository trainingPlanRepository;

    @PostMapping
    public TrainingPlan createPlan(
            @RequestBody TrainingPlan plan
    ) {
        return trainingPlanRepository.save(plan);
    }

    @GetMapping
    public List<TrainingPlan> getAllPlans() {

        return trainingPlanRepository.findAll();
    }
}