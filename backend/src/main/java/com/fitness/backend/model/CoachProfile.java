package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coach_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "specializations", columnDefinition = "TEXT")
    private String specializations; // e.g., "Weight Loss,Muscle Building,CrossFit"

    @Column(name = "certification")
    private String certification;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "max_clients")
    @Builder.Default
    private Integer maxClients = 10;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 0.0;

    // Relationship: Coach has many reviews
    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CoachReview> reviews = new ArrayList<>();

    // Helper method to get current number of clients
    public int getCurrentClientCount() {
        return user != null && user.getClients() != null ? user.getClients().size() : 0;
    }

    // Helper method to check if coach can accept more clients
    public boolean canAcceptClients() {
        return isAvailable && getCurrentClientCount() < maxClients;
    }
}