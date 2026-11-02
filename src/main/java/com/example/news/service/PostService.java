package com.example.news.service;

import com.example.news.dto.LikeResult;
import com.example.news.dto.PostForm;
import com.example.news.dto.PostView;

import java.util.List;


public interface PostService {

    List<PostView> feed(String username);

    List<PostView> allForAdmin();

    PostView published(Long id, String username);

    LikeResult toggleLike(Long postId, String username);

    void create(PostForm form, String username);

    void togglePublished(Long id);

    String delete(Long id);
}
