package com.fitness.backend.controller;

import com.fitness.backend.dto.ReviewRequest;
import com.fitness.backend.model.CoachReview;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.CoachReviewRepository;
import com.fitness.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin
public class CoachReviewController {

    private final CoachReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @PostMapping
    public CoachReview createReview(
            @RequestBody ReviewRequest request
    ) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        User coach = userRepository.findById(request.getCoachId())
                .orElseThrow(() ->
                        new RuntimeException("Coach not found"));

        CoachReview review = CoachReview.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .coach(coach)
                .build();

        return reviewRepository.save(review);
    }

    @GetMapping
    public List<CoachReview> getAllReviews() {

        return reviewRepository.findAll();
    }

    @GetMapping("/coach/{coachId}")
    public List<CoachReview> getCoachReviews(
            @PathVariable Long coachId
    ) {

        return reviewRepository.findAll()
                .stream()
                .filter(review ->
                        review.getCoach()
                                .getId()
                                .equals(coachId))
                .toList();
    }
}