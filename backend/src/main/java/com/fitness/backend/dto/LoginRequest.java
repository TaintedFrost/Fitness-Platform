// LoginRequest.java
package com.fitness.backend.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;     // was "username" — now matches User.email
    private String password;
}