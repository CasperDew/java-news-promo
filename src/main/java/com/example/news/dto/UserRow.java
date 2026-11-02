package com.example.news.dto;

import com.example.news.entity.Role;
import com.example.news.entity.User;

import java.util.List;

/**
 * Строка в списке пользователей админки.
 */
public record UserRow(User user, List<Role> roles, boolean muted, boolean banned) {
}
