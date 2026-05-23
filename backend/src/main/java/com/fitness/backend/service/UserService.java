package com.fitness.backend.service;

import com.fitness.backend.dto.LoginRequest;
import com.fitness.backend.dto.RegisterRequest;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setFitnessGoal(request.getFitnessGoal());
        user.setExperienceLevel(request.getExperienceLevel());

        userRepository.save(user);

        return "User registered successfully";
    }

    public User login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return null;
        }

        return user;
    }
}