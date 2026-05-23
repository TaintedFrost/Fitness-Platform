package com.fitness.backend.repository;

import com.fitness.backend.model.ProgressReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressReportRepository
        extends JpaRepository<ProgressReport, Long> {
}