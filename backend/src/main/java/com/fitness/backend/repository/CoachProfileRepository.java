package com.fitness.backend.repository;

import com.fitness.backend.model.CoachProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {
    List<CoachProfile> findByIsAvailableTrue();
}