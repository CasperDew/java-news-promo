package com.example.news.repository;

import com.example.news.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    @Query("select l.post.id, count(l) from PostLike l group by l.post.id")
    List<Object[]> countGroupedByPost();

    @Query("select l.post.id from PostLike l where l.user.id = :userId")
    Set<Long> findPostIdsLikedBy(@Param("userId") Long userId);

    @Modifying
    @Query("delete from PostLike l where l.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
