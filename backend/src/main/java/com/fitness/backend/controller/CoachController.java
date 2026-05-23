package com.fitness.backend.controller;

import com.fitness.backend.model.Coach;
import com.fitness.backend.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coaches")
@RequiredArgsConstructor
@CrossOrigin
public class CoachController {

    private final CoachService coachService;

    @PostMapping
    public Coach createCoach(@RequestBody Coach coach) {
        return coachService.createCoach(coach);
    }

    @GetMapping
    public List<Coach> getAllCoaches() {
        return coachService.getAllCoaches();
    }
}