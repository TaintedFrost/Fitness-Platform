package com.fitness.backend.controller;

import com.fitness.backend.model.CoachProfile;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.CoachProfileRepository;
import com.fitness.backend.service.CoachMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachMatchingService coachMatchingService;
    private final CoachProfileRepository coachProfileRepository;

    // GET /api/coaches/available
    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableCoaches() {
        List<CoachProfile> profiles = coachProfileRepository.findAvailableCoaches();
        return ResponseEntity.ok(profiles.stream().map(this::toSafeMap).toList());
    }

    // GET /api/coaches/match?userId=1
    @GetMapping("/match")
    public ResponseEntity<List<Map<String, Object>>> findMatchingCoaches(@RequestParam Long userId) {
        List<CoachProfile> matched = coachMatchingService.findMatchingCoaches(userId);
        return ResponseEntity.ok(matched.stream().map(this::toSafeMap).toList());
    }

    // POST /api/coaches/assign?userId=1&coachId=2
    @PostMapping("/assign")
    public ResponseEntity<?> assignCoach(
            @RequestParam Long userId,
            @RequestParam Long coachId) {
        coachMatchingService.assignCoachToUser(userId, coachId);
        return ResponseEntity.ok(Map.of("message", "Coach assigned successfully."));
    }

    // POST /api/coaches/unassign?userId=1
    @PostMapping("/unassign")
    public ResponseEntity<?> unassignCoach(@RequestParam Long userId) {
        coachMatchingService.unassignCoach(userId);
        return ResponseEntity.ok(Map.of("message", "Coach unassigned."));
    }

    // GET /api/coaches/clients?coachId=2
    @GetMapping("/clients")
    public ResponseEntity<List<Map<String, Object>>> getCoachClients(@RequestParam Long coachId) {
        List<User> clients = coachMatchingService.getCoachClients(coachId);
        List<Map<String, Object>> result = clients.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",              u.getId());
            m.put("fullName",        u.getFullName() != null ? u.getFullName() : "");
            m.put("email",           u.getEmail());
            m.put("fitnessGoals",    u.getFitnessGoals() != null ? u.getFitnessGoals() : "");
            m.put("experienceLevel", u.getExperienceLevel() != null ? u.getExperienceLevel() : "");
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // Safe serialization — breaks the CoachProfile <-> User circular reference
    private Map<String, Object> toSafeMap(CoachProfile c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",              c.getId());
        m.put("bio",             c.getBio() != null ? c.getBio() : "");
        m.put("specializations", c.getSpecializations() != null ? c.getSpecializations() : "");
        m.put("certification",   c.getCertification() != null ? c.getCertification() : "");
        m.put("yearsExperience", c.getYearsExperience() != null ? c.getYearsExperience() : 0);
        m.put("rating",          c.getRating() != null ? c.getRating() : 0.0);
        m.put("isAvailable",     c.getIsAvailable() != null ? c.getIsAvailable() : false);
        m.put("maxClients",      c.getMaxClients() != null ? c.getMaxClients() : 10);

        // Flatten user info to avoid circular reference
        if (c.getUser() != null) {
            m.put("user", Map.of(
                    "id",       c.getUser().getId(),
                    "fullName", c.getUser().getFullName() != null ? c.getUser().getFullName() : "",
                    "email",    c.getUser().getEmail()
            ));
        }
        return m;
    }
}