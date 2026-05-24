package com.fitness.backend.controller;

import com.fitness.backend.model.CoachProfile;
import com.fitness.backend.model.CoachReview;
import com.fitness.backend.model.User;
import com.fitness.backend.repository.CoachProfileRepository;
import com.fitness.backend.repository.CoachReviewRepository;
import com.fitness.backend.repository.UserRepository;
import com.fitness.backend.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CoachReviewController {

    private final CoachReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CoachProfileRepository coachProfileRepository;  // added

    @PostMapping
    public CoachReview createReview(@RequestBody ReviewRequest request) {

        User reviewer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CoachProfile coach = coachProfileRepository.findById(request.getCoachId())  // fixed — was userRepository
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        CoachReview review = CoachReview.builder()
                .user(reviewer)
                .coach(coach)                // now correct type: CoachProfile
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }

    @GetMapping
    public List<CoachReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/coach/{coachId}")
    public List<CoachReview> getCoachReviews(@PathVariable Long coachId) {
        return reviewRepository.findAll().stream()
                .filter(review -> review.getCoach() != null &&
                        review.getCoach().getId().equals(coachId))
                .collect(Collectors.toList());
    }
}