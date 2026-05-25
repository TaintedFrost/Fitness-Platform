package com.fitness.backend.controller;

import com.fitness.backend.model.*;
import com.fitness.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@CrossOrigin
public class ForumController {

    private final ForumPostRepository forumPostRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // ── GET all visible posts ─────────────────────────────
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllPosts() {
        List<ForumPost> posts = forumPostRepository.findAll().stream()
                .filter(p -> p.getIsHidden() == null || !p.getIsHidden())
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(posts.stream().map(p -> toPostMap(p, false)).toList());
    }

    // ── GET all posts (admin — includes hidden) ───────────
    @GetMapping("/admin/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPostsAdmin() {
        List<ForumPost> posts = forumPostRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(posts.stream().map(p -> toPostMap(p, true)).toList());
    }

    // ── GET single post with comments ────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        return forumPostRepository.findById(id).map(post -> {
            Map<String, Object> result = toPostMap(post, true);

            List<Map<String, Object>> comments = commentRepository.findAll().stream()
                    .filter(c -> c.getPost() != null && c.getPost().getId().equals(id))
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .map(this::toCommentMap)
                    .toList();

            result.put("comments", comments);
            return ResponseEntity.ok(result);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── CREATE post ───────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String title   = body.getOrDefault("title",   "").toString().trim();
            String content = body.getOrDefault("content", "").toString().trim();

            if (title.isBlank() || content.isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Title and content are required."));

            User author = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ForumPost post = ForumPost.builder()
                    .title(title)
                    .content(content)
                    .author(author)
                    .isHidden(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            forumPostRepository.save(post);
            return ResponseEntity.ok(toPostMap(post, false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    //ADD comment
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String content = body.getOrDefault("content", "").toString().trim();

            if (content.isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Comment cannot be empty."));

            ForumPost post = forumPostRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            User author = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Comment comment = Comment.builder()
                    .content(content)
                    .author(author)
                    .post(post)
                    .build();

            commentRepository.save(comment);
            return ResponseEntity.ok(toCommentMap(comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    //DELETE post (admin or author)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            // Delete comments first
            commentRepository.findAll().stream()
                    .filter(c -> c.getPost() != null && c.getPost().getId().equals(id))
                    .forEach(c -> commentRepository.deleteById(c.getId()));

            forumPostRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Post deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    //DELETE comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            commentRepository.deleteById(commentId);
            return ResponseEntity.ok(Map.of("message", "Comment deleted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── ADMIN: hide post ──────────────────────────────────
    @PostMapping("/{id}/hide")
    public ResponseEntity<?> hidePost(@PathVariable Long id) {
        return forumPostRepository.findById(id).map(post -> {
            post.setIsHidden(true);
            forumPostRepository.save(post);
            return ResponseEntity.ok(Map.of("message", "Post hidden."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── ADMIN: unhide post ────────────────────────────────
    @PostMapping("/{id}/unhide")
    public ResponseEntity<?> unhidePost(@PathVariable Long id) {
        return forumPostRepository.findById(id).map(post -> {
            post.setIsHidden(false);
            forumPostRepository.save(post);
            return ResponseEntity.ok(Map.of("message", "Post restored."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────
    private Map<String, Object> toPostMap(ForumPost p, boolean includeHidden) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",        p.getId());
        m.put("title",     p.getTitle() != null ? p.getTitle() : "");
        m.put("content",   p.getContent() != null ? p.getContent() : "");
        m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
        m.put("isHidden",  p.getIsHidden() != null && p.getIsHidden());

        if (p.getAuthor() != null) {
            m.put("author", Map.of(
                    "id",       p.getAuthor().getId(),
                    "fullName", p.getAuthor().getFullName() != null ? p.getAuthor().getFullName() : "",
                    "role",     p.getAuthor().getRole().name()
            ));
        } else {
            m.put("author", Map.of("id", 0, "fullName", "Anonymous", "role", "USER"));
        }

        // Comment count (without loading full comments)
        long commentCount = commentRepository.findAll().stream()
                .filter(c -> c.getPost() != null && c.getPost().getId().equals(p.getId()))
                .count();
        m.put("commentCount", commentCount);

        return m;
    }

    private Map<String, Object> toCommentMap(Comment c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",        c.getId());
        m.put("content",   c.getContent() != null ? c.getContent() : "");
        m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
        if (c.getAuthor() != null) {
            m.put("author", Map.of(
                    "id",       c.getAuthor().getId(),
                    "fullName", c.getAuthor().getFullName() != null ? c.getAuthor().getFullName() : "",
                    "role",     c.getAuthor().getRole().name()
            ));
        } else {
            m.put("author", Map.of("id", 0, "fullName", "Anonymous", "role", "USER"));
        }
        return m;
    }
}