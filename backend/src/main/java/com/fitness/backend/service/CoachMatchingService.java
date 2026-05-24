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

    public List<CoachProfile> getAvailableCoaches() {
        return coachProfileRepository.findAvailableCoaches(); // fixed method name
    }

    public List<CoachProfile> findMatchingCoaches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CoachProfile> available = coachProfileRepository.findAvailableCoaches();

        if (user.getFitnessGoals() == null || user.getFitnessGoals().isBlank()) {
            return available;
        }

        String goal = user.getFitnessGoals().toLowerCase();
        List<CoachProfile> matched = available.stream()
                .filter(c -> c.getSpecializations() != null &&
                        c.getSpecializations().toLowerCase().contains(goal))
                .collect(Collectors.toList());

        return matched.isEmpty() ? available : matched;
    }

    public User assignCoachToUser(Long userId, Long coachUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User coachUser = userRepository.findById(coachUserId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        user.setAssignedCoach(coachUser);
        return userRepository.save(user);
    }

    public User unassignCoach(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAssignedCoach(null);
        return userRepository.save(user);
    }

    public List<User> getCoachClients(Long coachUserId) {
        return userRepository.findByAssignedCoachId(coachUserId);
    }
}