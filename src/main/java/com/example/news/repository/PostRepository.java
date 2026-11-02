package com.example.news.repository;

import com.example.news.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublishedTrueOrderByCreatedAtDesc();

    List<Post> findAllByOrderByCreatedAtDesc();

    Optional<Post> findByIdAndPublishedTrue(Long id);
}
