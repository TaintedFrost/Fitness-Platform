package com.fitness.backend.dto;

import com.fitness.backend.model.Coach;
import com.fitness.backend.model.Progress;
import com.fitness.backend.model.TrainingPlan;
import com.fitness.backend.model.User;
import com.fitness.backend.model.WorkoutLog;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {

    private User user;

    private Coach assignedCoach;

    private List<WorkoutLog> workoutLogs;

    private List<Progress> progress;

    private List<TrainingPlan> trainingPlans;
}