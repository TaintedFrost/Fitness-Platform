package com.fitness.backend.controller;

import com.fitness.backend.model.ProgressReport;
import com.fitness.backend.model.User;
import com.fitness.backend.model.WorkoutLog;
import com.fitness.backend.repository.ProgressReportRepository;
import com.fitness.backend.repository.UserRepository;
import com.fitness.backend.repository.WorkoutLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin
public class ProgressController {

    private final WorkoutLogRepository workoutLogRepository;
    private final ProgressReportRepository progressReportRepository;
    private final UserRepository userRepository;

    @PostMapping("/{userId}")
    public ProgressReport generateReport(
            @PathVariable Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<WorkoutLog> logs =
                workoutLogRepository.findAll()
                        .stream()
                        .filter(log ->
                                log.getUser().getId().equals(userId))
                        .toList();

        int totalWorkouts = logs.size();

        int totalMinutes = logs.stream()
                .mapToInt(WorkoutLog::getDurationMinutes)
                .sum();

        ProgressReport report = ProgressReport.builder()
                .generatedDate(LocalDate.now())
                .totalWorkouts(totalWorkouts)
                .totalMinutes(totalMinutes)
                .summary("User completed "
                        + totalWorkouts
                        + " workouts")
                .user(user)
                .build();

        return progressReportRepository.save(report);
    }
}