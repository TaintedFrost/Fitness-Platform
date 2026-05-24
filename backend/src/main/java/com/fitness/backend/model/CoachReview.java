package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coach_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private CoachProfile coach;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;          // renamed from "reviewer" to match builder call in controller

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}