package com.example.news.dto;

import com.example.news.entity.Punishment;
import com.example.news.entity.Role;
import com.example.news.entity.User;

import java.util.List;

/**
 * Данные мини-окна пользователя. mute и ban равны null, если наказания нет.
 */
public record UserCard(User user, List<Role> roles, List<Role> missingRoles,
                       Punishment mute, Punishment ban, List<Punishment> history, boolean self) {
}
