package com.fitness.backend.service;

import com.fitness.backend.dto.LoginRequest;
import com.fitness.backend.dto.LoginResponse;
import com.fitness.backend.dto.RegisterRequest;
import com.fitness.backend.model.Role;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .fitnessGoals(request.getFitnessGoals())
                .experienceLevel(request.getExperienceLevel())
                .schedulePreference(request.getSchedulePreference())
                .role(Role.USER)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse(token, user.getRole().name(), user.getId(), user.getEmail(), user.getFullName());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse(token, user.getRole().name(), user.getId(), user.getEmail(), user.getFullName());
    }
}