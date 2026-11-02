package com.example.news.controller;

import com.example.news.dto.LikeResult;
import com.example.news.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class LikeApiController {
    private final PostService postService;

    @PostMapping("/api/posts/{id}/like")
    public LikeResult toggle(@PathVariable Long id, Principal principal) {
        return postService.toggleLike(id, principal.getName());
    }
}
