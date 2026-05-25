package com.fitness.backend.repository;

import com.fitness.backend.model.CoachApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CoachApplicationRepository extends JpaRepository<CoachApplication, Long> {
    List<CoachApplication> findByStatus(CoachApplication.ApplicationStatus status);
    Optional<CoachApplication> findByUserId(Long userId);
    boolean existsByUserIdAndStatus(Long userId, CoachApplication.ApplicationStatus status);
}