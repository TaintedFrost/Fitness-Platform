package com.fitness.backend.controller;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/coach-applications")
@RequiredArgsConstructor
public class CoachApplicationController {

    private final CoachApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CoachProfileRepository coachProfileRepository;

    // ── USER: Submit application ──────────────────────────
    @PostMapping
    public ResponseEntity<?> submitApplication(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.COACH) {
            return ResponseEntity.badRequest().body(Map.of("message", "You are already a coach."));
        }

        if (applicationRepository.existsByUserIdAndStatus(userId, CoachApplication.ApplicationStatus.PENDING)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You already have a pending application."));
        }

        CoachApplication app = CoachApplication.builder()
                .user(user)
                .bio(body.getOrDefault("bio", "").toString())
                .specializations(body.getOrDefault("specializations", "").toString())
                .certification(body.getOrDefault("certification", "").toString())
                .yearsExperience(Integer.valueOf(body.getOrDefault("yearsExperience", "0").toString()))
                .motivation(body.getOrDefault("motivation", "").toString())
                .status(CoachApplication.ApplicationStatus.PENDING)
                .build();

        applicationRepository.save(app);
        return ResponseEntity.ok(Map.of("message", "Application submitted successfully!"));
    }

    // ── USER: Check own application status ───────────────
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getApplicationStatus(@PathVariable Long userId) {
        return applicationRepository.findByUserId(userId)
                .map(app -> ResponseEntity.ok(Map.of(
                        "status",    app.getStatus().name(),
                        "adminNotes", app.getAdminNotes() != null ? app.getAdminNotes() : "",
                        "createdAt", app.getCreatedAt().toString()
                )))
                .orElse(ResponseEntity.ok(Map.of("status", "NONE")));
    }

    // ── ADMIN: View all applications ─────────────────────
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllApplications(
            @RequestParam(required = false) String status) {

        List<CoachApplication> apps = (status != null)
                ? applicationRepository.findByStatus(CoachApplication.ApplicationStatus.valueOf(status))
                : applicationRepository.findAll();

        List<Map<String, Object>> result = apps.stream().map(app -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",              app.getId());
            m.put("userId",          app.getUser().getId());
            m.put("fullName",        app.getUser().getFullName() != null ? app.getUser().getFullName() : "");
            m.put("email",           app.getUser().getEmail());
            m.put("bio",             app.getBio() != null ? app.getBio() : "");
            m.put("specializations", app.getSpecializations() != null ? app.getSpecializations() : "");
            m.put("certification",   app.getCertification() != null ? app.getCertification() : "");
            m.put("yearsExperience", app.getYearsExperience() != null ? app.getYearsExperience() : 0);
            m.put("motivation",      app.getMotivation() != null ? app.getMotivation() : "");
            m.put("status",          app.getStatus().name());
            m.put("adminNotes",      app.getAdminNotes() != null ? app.getAdminNotes() : "");
            m.put("createdAt",       app.getCreatedAt().toString());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // ── ADMIN: Approve application ────────────────────────
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveApplication(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {

        CoachApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // 1. Update application status
        app.setStatus(CoachApplication.ApplicationStatus.APPROVED);
        app.setReviewedAt(LocalDateTime.now());
        if (body != null && body.containsKey("adminNotes")) {
            app.setAdminNotes(body.get("adminNotes").toString());
        }
        applicationRepository.save(app);

        // 2. Upgrade user role to COACH
        User user = app.getUser();
        user.setRole(Role.COACH);
        userRepository.save(user);

        // 3. Create CoachProfile if it doesn't exist
        boolean profileExists = coachProfileRepository.findAll().stream()
                .anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()));

        if (!profileExists) {
            CoachProfile profile = CoachProfile.builder()
                    .user(user)
                    .bio(app.getBio())
                    .specializations(app.getSpecializations())
                    .certification(app.getCertification())
                    .yearsExperience(app.getYearsExperience())
                    .isAvailable(true)
                    .maxClients(10)
                    .rating(0.0)
                    .build();
            coachProfileRepository.save(profile);
        }

        return ResponseEntity.ok(Map.of("message", "Application approved. User is now a coach."));
    }

    // ── ADMIN: Reject application ─────────────────────────
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {

        CoachApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(CoachApplication.ApplicationStatus.REJECTED);
        app.setReviewedAt(LocalDateTime.now());
        if (body != null && body.containsKey("adminNotes")) {
            app.setAdminNotes(body.get("adminNotes").toString());
        }
        applicationRepository.save(app);

        return ResponseEntity.ok(Map.of("message", "Application rejected."));
    }
}