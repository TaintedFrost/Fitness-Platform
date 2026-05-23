package com.fitness.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate generatedDate;

    private int totalWorkouts;

    private int totalMinutes;

    private String summary;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}