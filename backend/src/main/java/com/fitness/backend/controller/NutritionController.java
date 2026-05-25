package com.fitness.backend.controller;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NutritionController {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final UserRepository userRepository;

    // ── GET user's nutrition plans ────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPlans(@PathVariable Long userId) {
        try {
            List<NutritionPlan> plans = nutritionPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(plans.stream().map(this::toMap).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── CREATE nutrition plan (coach or auto-generated) ───
    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            User user   = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            NutritionPlan plan = NutritionPlan.builder()
                    .user(user)
                    .title(body.getOrDefault("title", "Nutrition Plan").toString())
                    .fitnessGoalTarget(body.getOrDefault("fitnessGoalTarget", "").toString())
                    .description(body.getOrDefault("description", "").toString())
                    .dailyCalories(parseIntSafe(body.get("dailyCalories")))
                    .proteinGrams(parseIntSafe(body.get("proteinGrams")))
                    .carbsGrams(parseIntSafe(body.get("carbsGrams")))
                    .fatGrams(parseIntSafe(body.get("fatGrams")))
                    .waterLiters(parseDoubleSafe(body.get("waterLiters")))
                    .breakfast(body.getOrDefault("breakfast", "").toString())
                    .lunch(body.getOrDefault("lunch", "").toString())
                    .dinner(body.getOrDefault("dinner", "").toString())
                    .snacks(body.getOrDefault("snacks", "").toString())
                    .restrictions(body.getOrDefault("restrictions", "").toString())
                    .supplements(body.getOrDefault("supplements", "").toString())
                    .isActive(true)
                    .build();

            // Optional coach
            if (body.containsKey("coachId") && body.get("coachId") != null) {
                userRepository.findById(Long.valueOf(body.get("coachId").toString()))
                        .ifPresent(plan::setCoach);
            }

            nutritionPlanRepository.save(plan);
            return ResponseEntity.ok(toMap(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── AUTO-GENERATE based on fitness goal ───────────────
    @PostMapping("/generate/{userId}")
    public ResponseEntity<?> generatePlan(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String goal  = user.getFitnessGoals() != null
                    ? user.getFitnessGoals().toLowerCase() : "";
            String level = user.getExperienceLevel() != null
                    ? user.getExperienceLevel() : "BEGINNER";

            NutritionPlan plan = buildRecommendedPlan(user, goal, level);
            nutritionPlanRepository.save(plan);
            return ResponseEntity.ok(toMap(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE plan ───────────────────────────────────────
    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        try {
            nutritionPlanRepository.deleteById(planId);
            return ResponseEntity.ok(Map.of("message", "Plan deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Auto-generate logic aligned with fitness goal ─────
    private NutritionPlan buildRecommendedPlan(User user, String goal, String level) {
        String title; String desc;
        int calories; int protein; int carbs; int fat;
        String breakfast; String lunch; String dinner; String snacks; String supplements;

        if (goal.contains("weight loss") || goal.contains("lose weight") || goal.contains("cut")) {
            title    = "Weight Loss Nutrition Plan";
            desc     = "A calorie-controlled plan aligned with your weight loss goal. Focuses on high protein to preserve muscle while in a caloric deficit.";
            calories = level.equals("ADVANCED") ? 1800 : level.equals("INTERMEDIATE") ? 1600 : 1400;
            protein  = 160; carbs = 130; fat = 50;
            breakfast = "Greek yogurt with berries and a handful of almonds, black coffee or green tea";
            lunch     = "Grilled chicken breast salad with leafy greens, cucumber, tomato, olive oil and lemon dressing";
            dinner    = "Baked salmon with steamed broccoli and a small portion of quinoa";
            snacks    = "Apple with peanut butter, boiled eggs, or a protein shake";
            supplements = "Whey protein, Multivitamin, Omega-3, Vitamin D";

        } else if (goal.contains("muscle") || goal.contains("bulk") || goal.contains("strength")) {
            title    = "Muscle Building Nutrition Plan";
            desc     = "A high-calorie, high-protein plan to support muscle growth and strength gains aligned with your goals.";
            calories = level.equals("ADVANCED") ? 3200 : level.equals("INTERMEDIATE") ? 2800 : 2500;
            protein  = 200; carbs = 350; fat = 80;
            breakfast = "Oatmeal with banana, 4 scrambled eggs, whole milk, and a protein shake";
            lunch     = "Chicken rice bowl with brown rice, grilled chicken, avocado and mixed vegetables";
            dinner    = "Lean beef or turkey with sweet potato mash and steamed green beans";
            snacks    = "Cottage cheese with fruit, mixed nuts, rice cakes with peanut butter, protein shake";
            supplements = "Whey protein, Creatine monohydrate, Multivitamin, Omega-3, Vitamin D";

        } else if (goal.contains("endurance") || goal.contains("cardio") || goal.contains("run")) {
            title    = "Endurance Performance Nutrition Plan";
            desc     = "A carbohydrate-focused plan to fuel endurance training and support cardiovascular performance.";
            calories = level.equals("ADVANCED") ? 3000 : level.equals("INTERMEDIATE") ? 2600 : 2200;
            protein  = 150; carbs = 400; fat = 70;
            breakfast = "Whole grain toast with peanut butter and banana, orange juice, oatmeal with honey";
            lunch     = "Pasta with lean turkey mince tomato sauce, side salad, whole grain bread";
            dinner    = "Grilled chicken with brown rice, roasted sweet potato, and green vegetables";
            snacks    = "Energy bars, fruit smoothies, dried fruit and nuts, sports drinks during training";
            supplements = "Electrolytes, Whey protein, B-vitamins, Iron (if deficient), Vitamin C";

        } else {
            title    = "General Fitness Nutrition Plan";
            desc     = "A balanced nutrition plan to support your overall fitness and wellbeing.";
            calories = level.equals("ADVANCED") ? 2500 : level.equals("INTERMEDIATE") ? 2200 : 2000;
            protein  = 150; carbs = 250; fat = 70;
            breakfast = "Overnight oats with chia seeds and berries, two boiled eggs, coffee or tea";
            lunch     = "Grilled chicken or tuna wrap with whole grain tortilla, salad and hummus";
            dinner    = "Lean protein of choice with roasted vegetables and a complex carbohydrate";
            snacks    = "Fresh fruit, nuts, Greek yogurt, protein bars, vegetable sticks with hummus";
            supplements = "Whey protein, Multivitamin, Omega-3";
        }

        return NutritionPlan.builder()
                .user(user)
                .title(title)
                .fitnessGoalTarget(user.getFitnessGoals() != null ? user.getFitnessGoals() : "General Fitness")
                .description(desc)
                .dailyCalories(calories)
                .proteinGrams(protein)
                .carbsGrams(carbs)
                .fatGrams(fat)
                .waterLiters(3.0)
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .snacks(snacks)
                .supplements(supplements)
                .restrictions("")
                .isActive(true)
                .build();
    }

    private Map<String, Object> toMap(NutritionPlan p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",               p.getId());
        m.put("title",            p.getTitle()            != null ? p.getTitle()            : "");
        m.put("fitnessGoalTarget",p.getFitnessGoalTarget()!= null ? p.getFitnessGoalTarget(): "");
        m.put("description",      p.getDescription()      != null ? p.getDescription()      : "");
        m.put("dailyCalories",    p.getDailyCalories()    != null ? p.getDailyCalories()    : 0);
        m.put("proteinGrams",     p.getProteinGrams()     != null ? p.getProteinGrams()     : 0);
        m.put("carbsGrams",       p.getCarbsGrams()       != null ? p.getCarbsGrams()       : 0);
        m.put("fatGrams",         p.getFatGrams()         != null ? p.getFatGrams()         : 0);
        m.put("waterLiters",      p.getWaterLiters()      != null ? p.getWaterLiters()      : 0);
        m.put("breakfast",        p.getBreakfast()        != null ? p.getBreakfast()        : "");
        m.put("lunch",            p.getLunch()            != null ? p.getLunch()            : "");
        m.put("dinner",           p.getDinner()           != null ? p.getDinner()           : "");
        m.put("snacks",           p.getSnacks()           != null ? p.getSnacks()           : "");
        m.put("restrictions",     p.getRestrictions()     != null ? p.getRestrictions()     : "");
        m.put("supplements",      p.getSupplements()      != null ? p.getSupplements()      : "");
        m.put("isActive",         p.getIsActive()         != null ? p.getIsActive()         : false);
        m.put("createdAt",        p.getCreatedAt()        != null ? p.getCreatedAt().toString(): "");
        if (p.getCoach() != null)
            m.put("coachName", p.getCoach().getFullName() != null ? p.getCoach().getFullName() : "");
        return m;
    }

    private Integer parseIntSafe(Object val) {
        if (val == null || val.toString().isBlank()) return 0;
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private Double parseDoubleSafe(Object val) {
        if (val == null || val.toString().isBlank()) return 0.0;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}