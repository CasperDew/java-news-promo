package com.example.news.service;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MutedException extends RuntimeException {
    private final LocalDateTime until;

    public MutedException(LocalDateTime until) {
        super("Комментарии запрещены до " + until);
        this.until = until;
    }

}
