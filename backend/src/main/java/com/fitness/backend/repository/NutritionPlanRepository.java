package com.fitness.backend.repository;

import com.fitness.backend.model.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {
    List<NutritionPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NutritionPlan> findByCoachId(Long coachId);
}