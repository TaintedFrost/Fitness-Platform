package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String difficulty;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private User coach;

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL)
    private List<Exercise> exercises;
}