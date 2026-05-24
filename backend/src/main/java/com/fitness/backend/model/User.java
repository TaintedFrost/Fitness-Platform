package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // USER, COACH, ADMIN

    // Fitness profile data
    @Column(name = "fitness_goals", columnDefinition = "TEXT")
    private String fitnessGoals;

    @Column(name = "experience_level")
    private String experienceLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(name = "schedule_preference")
    private String schedulePreference;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship: User assigned to one coach
    @ManyToOne
    @JoinColumn(name = "assigned_coach_id")
    private User assignedCoach;

    // Relationship: Coach has many clients (inverse of above)
    @OneToMany(mappedBy = "assignedCoach")
    @Builder.Default
    private List<User> clients = new ArrayList<>();

    // Relationship: User has one coach profile (if they are a coach)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CoachProfile coachProfile;

    // Relationship: User has many workout plans
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkoutPlan> workoutPlans = new ArrayList<>();

    // Relationship: User has many workout logs
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WorkoutLog> workoutLogs = new ArrayList<>();

    // Relationship: User has many progress reports
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProgressReport> progressReports = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to check if user is a coach
    public boolean isCoach() {
        return this.role == Role.COACH;
    }

    // Helper method to check if user has an assigned coach
    public boolean hasAssignedCoach() {
        return this.assignedCoach != null;
    }
}