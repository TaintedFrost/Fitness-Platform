package com.fitness.backend.service;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.ExerciseRepository;
import com.fitness.backend.repository.UserRepository;
import com.fitness.backend.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    /**
     * Business Process: Workout Plan Creation and Delivery
     * Inputs: User fitness profile, User goals, Coach expertise
     * Method: Coach reviews profile, designs plan, uploads exercises, system publishes
     * Output: Personalized workout plan, Training schedule available to user
     */
    @Transactional
    public WorkoutPlan createWorkoutPlan(Long coachId, Long clientId, WorkoutPlanDTO planDTO) {
        log.info("Coach {} creating workout plan for client {}", coachId, clientId);

        // Validate coach
        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        if (coach.getRole() != Role.COACH) {
            throw new RuntimeException("Only coaches can create workout plans");
        }

        // Validate client
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Verify coaching relationship
        if (client.getAssignedCoach() == null || !client.getAssignedCoach().getId().equals(coachId)) {
            throw new RuntimeException("Client is not assigned to this coach");
        }

        // Deactivate old plans
        List<WorkoutPlan> oldPlans = workoutPlanRepository.findByUserIdAndIsActiveTrue(clientId);
        oldPlans.forEach(plan -> plan.setIsActive(false));
        workoutPlanRepository.saveAll(oldPlans);

        // Create new workout plan
        WorkoutPlan workoutPlan = WorkoutPlan.builder()
                .name(planDTO.getName())
                .description(planDTO.getDescription())
                .user(client)
                .coach(coach)
                .startDate(planDTO.getStartDate() != null ? planDTO.getStartDate() : LocalDateTime.now())
                .durationWeeks(planDTO.getDurationWeeks())
                .difficultyLevel(mapDifficultyLevel(client.getExperienceLevel()))
                .isActive(true)
                .build();

        // Calculate end date
        if (planDTO.getDurationWeeks() != null) {
            workoutPlan.setEndDate(workoutPlan.getStartDate().plusWeeks(planDTO.getDurationWeeks()));
        }

        workoutPlan = workoutPlanRepository.save(workoutPlan);
        log.info("Created workout plan {} for client {}", workoutPlan.getId(), clientId);

        return workoutPlan;
    }

    /**
     * Add exercise to workout plan
     */
    @Transactional
    public Exercise addExerciseToWorkoutPlan(Long workoutPlanId, ExerciseDTO exerciseDTO) {
        WorkoutPlan workoutPlan = workoutPlanRepository.findById(workoutPlanId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        Exercise exercise = Exercise.builder()
                .workoutPlan(workoutPlan)
                .name(exerciseDTO.getName())
                .description(exerciseDTO.getDescription())
                .instructions(exerciseDTO.getInstructions())
                .sets(exerciseDTO.getSets())
                .reps(exerciseDTO.getReps())
                .durationMinutes(exerciseDTO.getDurationMinutes())
                .restSeconds(exerciseDTO.getRestSeconds())
                .dayOfWeek(exerciseDTO.getDayOfWeek())
                .orderIndex(exerciseDTO.getOrderIndex())
                .muscleGroup(exerciseDTO.getMuscleGroup())
                .exerciseType(exerciseDTO.getExerciseType())
                .build();

        return exerciseRepository.save(exercise);
    }

    /**
     * Get workout plan with exercises for a user
     */
    @Transactional(readOnly = true)
    public WorkoutPlan getActiveWorkoutPlan(Long userId) {
        return workoutPlanRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active workout plan found"));
    }

    /**
     * Get all workout plans for a user
     */
    @Transactional(readOnly = true)
    public List<WorkoutPlan> getAllUserWorkoutPlans(Long userId) {
        return workoutPlanRepository.findByUserId(userId);
    }

    /**
     * Update workout plan
     */
    @Transactional
    public WorkoutPlan updateWorkoutPlan(Long planId, WorkoutPlanDTO planDTO) {
        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

        if (planDTO.getName() != null) plan.setName(planDTO.getName());
        if (planDTO.getDescription() != null) plan.setDescription(planDTO.getDescription());
        if (planDTO.getDurationWeeks() != null) {
            plan.setDurationWeeks(planDTO.getDurationWeeks());
            plan.setEndDate(plan.getStartDate().plusWeeks(planDTO.getDurationWeeks()));
        }

        return workoutPlanRepository.save(plan);
    }

    /**
     * Delete exercise from workout plan
     */
    @Transactional
    public void deleteExercise(Long exerciseId) {
        exerciseRepository.deleteById(exerciseId);
    }

    /**
     * Map experience level to difficulty level
     */
    private WorkoutPlan.DifficultyLevel mapDifficultyLevel(String experienceLevel) {
        if (experienceLevel == null) return WorkoutPlan.DifficultyLevel.BEGINNER;

        return switch (experienceLevel.toUpperCase()) {
            case "ADVANCED" -> WorkoutPlan.DifficultyLevel.ADVANCED;
            case "INTERMEDIATE" -> WorkoutPlan.DifficultyLevel.INTERMEDIATE;
            default -> WorkoutPlan.DifficultyLevel.BEGINNER;
        };
    }

    // DTOs
    public static class WorkoutPlanDTO {
        private String name;
        private String description;
        private LocalDateTime startDate;
        private Integer durationWeeks;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public Integer getDurationWeeks() { return durationWeeks; }
        public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
    }

    public static class ExerciseDTO {
        private String name;
        private String description;
        private String instructions;
        private Integer sets;
        private Integer reps;
        private Integer durationMinutes;
        private Integer restSeconds;
        private Integer dayOfWeek;
        private Integer orderIndex;
        private String muscleGroup;
        private Exercise.ExerciseType exerciseType;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        public Integer getSets() { return sets; }
        public void setSets(Integer sets) { this.sets = sets; }
        public Integer getReps() { return reps; }
        public void setReps(Integer reps) { this.reps = reps; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Integer getRestSeconds() { return restSeconds; }
        public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }
        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public String getMuscleGroup() { return muscleGroup; }
        public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
        public Exercise.ExerciseType getExerciseType() { return exerciseType; }
        public void setExerciseType(Exercise.ExerciseType exerciseType) { this.exerciseType = exerciseType; }
    }
}