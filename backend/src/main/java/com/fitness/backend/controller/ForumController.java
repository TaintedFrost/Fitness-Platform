package com.fitness.backend.controller;

import com.fitness.backend.model.ForumPost;
import com.fitness.backend.repository.ForumPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@CrossOrigin
public class ForumController {

    private final ForumPostRepository forumPostRepository;

    @PostMapping
    public ForumPost createPost(
            @RequestBody ForumPost post
    ) {

        post.setCreatedAt(LocalDateTime.now());

        return forumPostRepository.save(post);
    }

    @GetMapping
    public List<ForumPost> getAllPosts() {

        return forumPostRepository.findAll();
    }

    @GetMapping("/{id}")
    public ForumPost getPost(
            @PathVariable Long id
    ) {

        return forumPostRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));
    }
}