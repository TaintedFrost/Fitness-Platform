package com.fitness.backend.controller;

import com.fitness.backend.dto.LoginRequest;
import com.fitness.backend.dto.LoginResponse;
import com.fitness.backend.dto.RegisterRequest;
import com.fitness.backend.model.User;
import com.fitness.backend.service.JwtService;
import com.fitness.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        userService.register(request);

        return "User registered successfully";
    }

    @PostMapping("/login")
    public Object login(@RequestBody LoginRequest request) {

        User user = userService.login(request);

        if (user == null) {
            return "Invalid email or password";
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token);
    }
}