package com.example.news.service;

import com.example.news.entity.Comment;

import java.util.List;

public interface CommentService {

    List<Comment> visibleFor(Long postId);

    void add(Long postId, String username, String text);

    List<Comment> forModeration(String status);

    void setApproved(Long id, boolean approved);

    void delete(Long id);

}
