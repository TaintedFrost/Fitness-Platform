package com.fitness.backend.controller;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final CoachProfileRepository coachProfileRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
                "id", user.getId(),
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "fitnessGoals", user.getFitnessGoals() != null ? user.getFitnessGoals() : "",
                "experienceLevel", user.getExperienceLevel() != null ? user.getExperienceLevel() : ""
        ));

        // Assigned coach
        if (user.getAssignedCoach() != null) {
            User coachUser = user.getAssignedCoach();
            Map<String, Object> coachData = new HashMap<>();
            coachData.put("id", coachUser.getId());
            coachData.put("fullName", coachUser.getFullName() != null ? coachUser.getFullName() : "");
            coachData.put("email", coachUser.getEmail());
            if (coachUser.getCoachProfile() != null) {
                coachData.put("bio", coachUser.getCoachProfile().getBio());
                coachData.put("specializations", coachUser.getCoachProfile().getSpecializations());
                coachData.put("rating", coachUser.getCoachProfile().getRating());
            }
            response.put("coach", coachData);
        } else {
            response.put("coach", null);
        }

        // Workout plans
        List<Map<String, Object>> plans = new ArrayList<>();
        for (WorkoutPlan plan : workoutPlanRepository.findByUserId(userId)) {
            plans.add(Map.of(
                    "id", plan.getId(),
                    "name", plan.getName(),
                    "description", plan.getDescription() != null ? plan.getDescription() : "",
                    "difficultyLevel", plan.getDifficultyLevel() != null ? plan.getDifficultyLevel().name() : "",
                    "durationWeeks", plan.getDurationWeeks() != null ? plan.getDurationWeeks() : 0,
                    "isActive", plan.getIsActive() != null ? plan.getIsActive() : false
            ));
        }
        response.put("workoutPlans", plans);

        // Recent logs (last 5)
        List<WorkoutLog> allLogs = workoutLogRepository.findAll().stream()
                .filter(l -> l.getUser() != null && l.getUser().getId().equals(userId))
                .sorted((a, b) -> b.getWorkoutDate().compareTo(a.getWorkoutDate()))
                .limit(5)
                .toList();

        List<Map<String, Object>> logs = new ArrayList<>();
        for (WorkoutLog log : allLogs) {
            logs.add(Map.of(
                    "id", log.getId(),
                    "workoutDate", log.getWorkoutDate().toString(),
                    "durationMinutes", log.getDurationMinutes() != null ? log.getDurationMinutes() : 0,
                    "caloriesBurned", log.getCaloriesBurned() != null ? log.getCaloriesBurned() : 0,
                    "notes", log.getNotes() != null ? log.getNotes() : ""
            ));
        }
        response.put("recentLogs", logs);
        response.put("totalWorkouts", allLogs.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/coach/{userId}")
    public ResponseEntity<Map<String, Object>> getCoachDashboard(@PathVariable Long userId) {
        User coach = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("coach", Map.of(
                "id", coach.getId(),
                "fullName", coach.getFullName() != null ? coach.getFullName() : "",
                "email", coach.getEmail()
        ));

        // Clients
        List<User> clients = userRepository.findByAssignedCoachId(userId);
        List<Map<String, Object>> clientData = new ArrayList<>();
        for (User client : clients) {
            clientData.add(Map.of(
                    "id", client.getId(),
                    "fullName", client.getFullName() != null ? client.getFullName() : "",
                    "email", client.getEmail(),
                    "fitnessGoals", client.getFitnessGoals() != null ? client.getFitnessGoals() : "",
                    "experienceLevel", client.getExperienceLevel() != null ? client.getExperienceLevel() : ""
            ));
        }
        response.put("clients", clientData);
        response.put("clientCount", clientData.size());

        // Coach profile
        List<CoachProfile> profiles = coachProfileRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .toList();
        if (!profiles.isEmpty()) {
            CoachProfile profile = profiles.get(0);
            response.put("profile", Map.of(
                    "bio", profile.getBio() != null ? profile.getBio() : "",
                    "specializations", profile.getSpecializations() != null ? profile.getSpecializations() : "",
                    "rating", profile.getRating(),
                    "yearsExperience", profile.getYearsExperience() != null ? profile.getYearsExperience() : 0,
                    "isAvailable", profile.getIsAvailable()
            ));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        List<User> allUsers = userRepository.findAll();
        long userCount = allUsers.stream().filter(u -> u.getRole().name().equals("USER")).count();
        long coachCount = allUsers.stream().filter(u -> u.getRole().name().equals("COACH")).count();

        List<Map<String, Object>> users = new ArrayList<>();
        for (User u : allUsers) {
            users.add(Map.of(
                    "id", u.getId(),
                    "fullName", u.getFullName() != null ? u.getFullName() : "",
                    "email", u.getEmail(),
                    "role", u.getRole().name(),
                    "hasCoach", u.getAssignedCoach() != null
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", userCount);
        response.put("totalCoaches", coachCount);
        response.put("totalAccounts", allUsers.size());
        response.put("users", users);

        return ResponseEntity.ok(response);
    }
}