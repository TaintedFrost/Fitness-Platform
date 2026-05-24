package com.fitness.backend.service;

import com.fitness.backend.model.User;
import com.fitness.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoachAssignmentService {

    private final UserRepository userRepository;

    public void assignCoach(Long clientId, Long coachId) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        if (!coach.getRole().equals("COACH")) {
            throw new RuntimeException("Selected user is not a coach");
        }

        client.setAssignedCoach(coach);

        userRepository.save(client);
    }
}