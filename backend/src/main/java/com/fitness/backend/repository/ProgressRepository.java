package com.fitness.backend.repository;

import com.fitness.backend.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByUserId(Long userId);
}