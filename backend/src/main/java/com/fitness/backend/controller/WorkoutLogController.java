package com.fitness.backend.controller;

import com.fitness.backend.model.WorkoutLog;
import com.fitness.backend.repository.WorkoutLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin
public class WorkoutLogController {

    private final WorkoutLogRepository workoutLogRepository;

    @PostMapping
    public WorkoutLog createLog(
            @RequestBody WorkoutLog log
    ) {

        return workoutLogRepository.save(log);
    }

    @GetMapping
    public List<WorkoutLog> getAllLogs() {

        return workoutLogRepository.findAll();
    }
}