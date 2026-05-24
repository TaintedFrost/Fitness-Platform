package com.fitness.backend.repository;

import com.fitness.backend.model.CoachProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {

    // Use @Query to avoid Spring Data misreading the "is" prefix on isAvailable
    @Query("SELECT c FROM CoachProfile c WHERE c.isAvailable = true")
    List<CoachProfile> findAvailableCoaches();
}