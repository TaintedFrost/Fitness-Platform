package com.fitness.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    private Long userId;

    private Long coachId;

    private int rating;

    private String comment;
}