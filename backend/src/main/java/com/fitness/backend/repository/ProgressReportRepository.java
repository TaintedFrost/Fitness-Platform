package com.fitness.backend.repository;

import com.fitness.backend.model.ProgressReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {

    List<ProgressReport> findByUserId(Long userId);

    List<ProgressReport> findByUserIdOrderByReportDateDesc(Long userId);

    List<ProgressReport> findByUserIdAndReportDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}