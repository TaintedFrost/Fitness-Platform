// RegisterRequest.java
package com.fitness.backend.dto;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String fitnessGoals;
    private String experienceLevel;
    private String schedulePreference;
}