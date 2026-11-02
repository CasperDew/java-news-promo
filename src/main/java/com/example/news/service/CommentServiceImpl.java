package com.example.news.service;

import com.example.news.entity.Comment;
import com.example.news.entity.Post;
import com.example.news.entity.User;
import com.example.news.repository.CommentRepository;
import com.example.news.repository.PostRepository;
import com.example.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository comments;
    private final PostRepository posts;
    private final UserRepository users;

    private final PunishmentService punishments;

    @Override
    @Transactional(readOnly = true)
    public List<Comment> visibleFor(Long postId) {
        return comments.findByPostIdAndApprovedTrueOrderByCreatedAtAsc(postId);
    }

    @Override
    @Transactional
    public void add(Long postId, String username, String text) {
        punishments.muteUntil(username).ifPresent(until -> {
            throw new MutedException(until);
        });
        Post post = posts.findByIdAndPublishedTrue(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        comments.save(new Comment(post, user, text.trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> forModeration(String status) {
        return switch (status) {
            case "approved" -> comments.findByApprovedOrderByCreatedAtDesc(true);
            case "hidden" -> comments.findByApprovedOrderByCreatedAtDesc(false);
            default -> comments.findAllByOrderByCreatedAtDesc();
        };
    }

    @Override
    @Transactional
    public void setApproved(Long id, boolean approved) {
        comments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .setApproved(approved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        comments.deleteById(id);
    }
}
