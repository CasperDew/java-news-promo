package com.example.news.dto;

import com.example.news.entity.Post;

public record PostView(Post post, long likes, long comments, boolean liked) {
}
