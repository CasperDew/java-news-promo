package com.example.news.service;

import com.example.news.dto.LikeResult;
import com.example.news.dto.PostForm;
import com.example.news.dto.PostView;
import com.example.news.entity.Post;
import com.example.news.entity.PostLike;
import com.example.news.entity.User;
import com.example.news.repository.CommentRepository;
import com.example.news.repository.PostLikeRepository;
import com.example.news.repository.PostRepository;
import com.example.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository posts;
    private final PostLikeRepository likes;
    private final CommentRepository comments;
    private final UserRepository users;

    @Override
    @Transactional(readOnly = true)
    public List<PostView> feed(String username) {
        return toViews(posts.findByPublishedTrueOrderByCreatedAtDesc(), username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostView> allForAdmin() {
        return toViews(posts.findAllByOrderByCreatedAtDesc(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public PostView published(Long id, String username) {
        Post post = posts.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return toViews(List.of(post), username).get(0);
    }

    @Override
    @Transactional
    public LikeResult toggleLike(Long postId, String username) {
        Post post = posts.findByIdAndPublishedTrue(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = currentUser(username);
        Optional<PostLike> existing = likes.findByUserIdAndPostId(user.getId(), postId);
        boolean liked;
        if (existing.isPresent()) {
            likes.delete(existing.get());
            liked = false;
        } else {
            likes.save(new PostLike(post, user));
            liked = true;
        }
        likes.flush();
        return new LikeResult(liked, likes.countByPostId(postId));
    }

    @Override
    @Transactional
    public void create(PostForm form, String username) {
        String image = form.getImageUrl() == null || form.getImageUrl().isBlank() ? null : form.getImageUrl();
        posts.save(new Post(form.getTitle().trim(), form.getSummary().trim(), form.getContent().trim(),
                image, form.isPublished(), currentUser(username)));
    }

    @Override
    @Transactional
    public void togglePublished(Long id) {
        Post post = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        post.setPublished(!post.isPublished());
    }

    /**
     * Удаляет пост и возвращает ссылку на его картинку, чтобы можно было удалить загруженный файл.
     */
    @Override
    @Transactional
    public String delete(Long id) {
        String image = posts.findById(id).map(Post::getImageUrl).orElse(null);
        likes.deleteByPostId(id);
        comments.deleteByPostId(id);
        posts.deleteById(id);
        return image;
    }

    User currentUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private List<PostView> toViews(List<Post> list, String username) {
        Map<Long, Long> likeCounts = toMap(likes.countGroupedByPost());
        Map<Long, Long> commentCounts = toMap(comments.countVisibleByPost());
        Set<Long> liked = username == null ? Set.of()
                : users.findByUsername(username).map(u -> likes.findPostIdsLikedBy(u.getId())).orElse(Set.of());
        return list.stream().map(p -> new PostView(p,
                likeCounts.getOrDefault(p.getId(), 0L),
                commentCounts.getOrDefault(p.getId(), 0L),
                liked.contains(p.getId()))).toList();
    }

    private static Map<Long, Long> toMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) map.put((Long) r[0], (Long) r[1]);
        return map;
    }
}
