package com.fitness.backend.repository;

import com.fitness.backend.model.CoachProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachProfileRepository
        extends JpaRepository<CoachProfile, Long> {
}