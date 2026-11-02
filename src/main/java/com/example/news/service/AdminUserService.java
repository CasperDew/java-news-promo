package com.example.news.service;

import com.example.news.dto.UserCard;
import com.example.news.dto.UserRow;
import com.example.news.entity.Role;

import java.util.List;
import java.util.Optional;

public interface AdminUserService {
    List<UserRow> search(String query);

    UserCard card(Long id, String adminName);

    Optional<UserCard> findCard(Long id, String adminName);

    void addRole(Long id, Role role);

    void removeRole(Long id, Role role, String adminName);

    void mute(Long id, int amount, String unit, String reason, String adminName);

    void ban(Long id, int days, String reason, String adminName);

    void revoke(Long userId, Long punishmentId, String adminName);

}
