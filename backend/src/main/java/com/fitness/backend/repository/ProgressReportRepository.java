package com.fitness.backend.repository;

import com.fitness.backend.model.ProgressReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {
    List<ProgressReport> findByUserIdOrderByReportDateDesc(Long userId);
}