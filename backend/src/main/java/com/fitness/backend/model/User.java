package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String fitnessGoal;

    private String experienceLevel;

    private String bio;

    @OneToMany(mappedBy = "coach")
    private List<WorkoutPlan> workoutPlans;

    @OneToMany(mappedBy = "author")
    private List<ForumPost> posts;

    @OneToOne
    @JoinColumn(name = "assigned_coach_id")
    private User assignedCoach;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CoachProfile coachProfile;

    @OneToMany(mappedBy = "user")
    private List<WorkoutLog> workoutLogs;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ProgressReport progressReport;

    @OneToMany(mappedBy = "coach")
    private List<CoachReview> receivedReviews;

    @OneToMany(mappedBy = "user")
    private List<CoachReview> writtenReviews;
}