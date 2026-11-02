package com.example.news.repository;

import com.example.news.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndApprovedTrueOrderByCreatedAtAsc(Long postId);

    List<Comment> findAllByOrderByCreatedAtDesc();

    List<Comment> findByApprovedOrderByCreatedAtDesc(boolean approved);

    @Query("select c.post.id, count(c) from Comment c where c.approved = true group by c.post.id")
    List<Object[]> countVisibleByPost();

    @Modifying
    @Query("delete from Comment c where c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
