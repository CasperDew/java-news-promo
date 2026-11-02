package com.example.news.service;

import com.example.news.entity.Punishment;
import com.example.news.entity.PunishmentType;
import com.example.news.entity.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PunishmentService {

    Optional<Punishment> active(Long userId, PunishmentType type);

    boolean isBanned(Long userId);

    Optional<LocalDateTime> muteUntil(String username);

    Set<Long> activeUserIds(PunishmentType type);

    List<Punishment> history(Long userId);

    void issue(User user, PunishmentType type, Duration duration, String term, String reason, User admin);

    void revoke(Long userId, Long punishmentId, User admin);
}
