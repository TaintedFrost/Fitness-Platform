package com.fitness.backend.dto;

import com.fitness.backend.model.Progress;
import com.fitness.backend.model.User;
import com.fitness.backend.model.WorkoutLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fitness.backend.model.CoachProfile;   // replaces Coach
import com.fitness.backend.model.TrainingPlan;    // now exists after Fix 1

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {

    private User user;

    private CoachProfile assignedCoach;

    private List<WorkoutLog> workoutLogs;

    private List<Progress> progress;

    private List<TrainingPlan> trainingPlans;
}