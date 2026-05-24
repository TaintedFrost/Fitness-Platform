package com.fitness.backend.service;

import com.fitness.backend.dto.DashboardResponse;
import com.fitness.backend.model.*;
import com.fitness.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final CoachProfileRepository coachProfileRepository;  // fixed
    private final WorkoutLogRepository workoutLogRepository;
    private final ProgressRepository progressRepository;
    private final TrainingPlanRepository trainingPlanRepository;

    public DashboardResponse getDashboard(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CoachProfile> coaches = coachProfileRepository.findAll();  // fixed

        List<WorkoutLog> logs = workoutLogRepository.findAll();

        List<Progress> progress = progressRepository.findAll();

        List<TrainingPlan> plans = trainingPlanRepository.findAll();

        CoachProfile coach = coaches.isEmpty() ? null : coaches.get(0);  // fixed

        return new DashboardResponse(
                user,
                coach,
                logs,
                progress,
                plans
        );
    }
}