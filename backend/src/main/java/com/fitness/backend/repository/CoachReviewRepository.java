package com.fitness.backend.repository;

import com.fitness.backend.model.CoachReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachReviewRepository
        extends JpaRepository<CoachReview, Long> {
}