package com.fitness.backend;

import com.fitness.backend.model.Role;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@mail.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin"))
                    .fullName("Platform Admin")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin account created: admin@mail.com / admin");
        } else {
            System.out.println("ℹ️ Admin account already exists.");
        }
    }
}