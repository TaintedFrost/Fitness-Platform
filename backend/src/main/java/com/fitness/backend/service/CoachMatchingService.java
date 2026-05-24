package com.fitness.backend.service;

import com.fitness.backend.model.CoachProfile;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.CoachProfileRepository;
import com.fitness.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachMatchingService {

    private final CoachProfileRepository coachProfileRepository;
    private final UserRepository userRepository;

    // Called by CoachController — returns coaches whose specialization
    // is relevant to the requesting user's fitnessGoals
    public List<CoachProfile> findMatchingCoaches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CoachProfile> available = coachProfileRepository.findByIsAvailableTrue();

        if (user.getFitnessGoals() == null || user.getFitnessGoals().isBlank()) {
            return available;
        }

        // Filter by specialization matching user's fitness goals (case-insensitive)
        String goal = user.getFitnessGoals().toLowerCase();
        List<CoachProfile> matched = available.stream()
                .filter(c -> c.getSpecializations() != null &&
                        c.getSpecializations().toLowerCase().contains(goal))
                .collect(Collectors.toList());

        // Fall back to all available coaches if no specialization match
        return matched.isEmpty() ? available : matched;
    }

    // Called by CoachController — assigns a coach (by their User id) to a user
    public User assignCoachToUser(Long userId, Long coachUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User coachUser = userRepository.findById(coachUserId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        if (coachUser.getCoachProfile() == null) {
            throw new RuntimeException("Target user is not a coach");
        }

        user.setAssignedCoach(coachUser);
        return userRepository.save(user);
    }

    // Called by CoachController — removes coach assignment from a user
    public User unassignCoach(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAssignedCoach(null);
        return userRepository.save(user);
    }

    // Called by CoachController — returns all clients assigned to a coach
    public List<User> getCoachClients(Long coachUserId) {
        return userRepository.findByAssignedCoachId(coachUserId);
    }

    // General helpers used elsewhere
    public List<CoachProfile> getAvailableCoaches() {
        return coachProfileRepository.findByIsAvailableTrue();
    }
}