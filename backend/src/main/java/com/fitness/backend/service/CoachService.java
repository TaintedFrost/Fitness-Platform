package com.fitness.backend.service;

import com.fitness.backend.model.Coach;
import com.fitness.backend.repository.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository coachRepository;

    public Coach createCoach(Coach coach) {
        return coachRepository.save(coach);
    }

    public List<Coach> getAllCoaches() {
        return coachRepository.findAll();
    }
}