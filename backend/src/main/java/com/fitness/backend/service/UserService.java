package com.fitness.backend.service;

import com.fitness.backend.model.User;
import com.fitness.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, String email, String fullName, String fitnessGoals) {
        User user = getUserById(id);
        if (email != null)        user.setEmail(email);
        if (fullName != null)     user.setFullName(fullName);
        if (fitnessGoals != null) user.setFitnessGoals(fitnessGoals);  // plural — matches User.fitnessGoals
        return userRepository.save(user);
    }
}