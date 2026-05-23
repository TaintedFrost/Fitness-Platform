//package com.fitness.backend.dto;
//
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class RegisterRequest {
//
//    private String username;
//    private String email;
//    private String password;
//    private String fitnessGoal;
//    private String experienceLevel;
//
//}
package com.fitness.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;

    private String email;

    private String password;

    private String fitnessGoal;

    private String experienceLevel;
}