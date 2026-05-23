package com.fitness.backend.repository;

import com.fitness.backend.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumPostRepository
        extends JpaRepository<ForumPost, Long> {
}