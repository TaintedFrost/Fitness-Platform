package com.fitness.backend.controller;

import com.fitness.backend.model.CoachProfile;
import com.fitness.backend.model.User;
import com.fitness.backend.service.CoachMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachMatchingService coachMatchingService;

    // GET /api/coaches/match?userId=1
    // Returns coaches matched to a user's fitness goals
    @GetMapping("/match")
    public ResponseEntity<List<CoachProfile>> findMatchingCoaches(@RequestParam Long userId) {
        return ResponseEntity.ok(coachMatchingService.findMatchingCoaches(userId));
    }

    // POST /api/coaches/assign?userId=1&coachId=2
    // Assigns a coach to a user
    @PostMapping("/assign")
    public ResponseEntity<User> assignCoach(
            @RequestParam Long userId,
            @RequestParam Long coachId) {
        return ResponseEntity.ok(coachMatchingService.assignCoachToUser(userId, coachId));
    }

    // POST /api/coaches/unassign?userId=1
    // Removes coach assignment from a user
    @PostMapping("/unassign")
    public ResponseEntity<User> unassignCoach(@RequestParam Long userId) {
        return ResponseEntity.ok(coachMatchingService.unassignCoach(userId));
    }

    // GET /api/coaches/clients?coachId=2
    // Returns all clients assigned to a coach
    @GetMapping("/clients")
    public ResponseEntity<List<User>> getCoachClients(@RequestParam Long coachId) {
        return ResponseEntity.ok(coachMatchingService.getCoachClients(coachId));
    }

    // GET /api/coaches/available
    // Returns all available coaches
    @GetMapping("/available")
    public ResponseEntity<List<CoachProfile>> getAvailableCoaches() {
        return ResponseEntity.ok(coachMatchingService.getAvailableCoaches());
    }
}