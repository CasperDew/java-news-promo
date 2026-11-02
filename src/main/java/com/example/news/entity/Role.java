package com.example.news.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    USER("Пользователь"),
    ADMIN("Администратор");

    private final String label;
}
