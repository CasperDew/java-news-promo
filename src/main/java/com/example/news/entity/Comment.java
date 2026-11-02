package com.example.news.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private boolean approved = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Comment(Post post, User author, String text) {
        this.post = post;
        this.author = author;
        this.text = text;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
