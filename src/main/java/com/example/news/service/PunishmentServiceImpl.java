package com.example.news.service;

import com.example.news.entity.Punishment;
import com.example.news.entity.PunishmentType;
import com.example.news.entity.User;
import com.example.news.repository.PunishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PunishmentServiceImpl implements PunishmentService {
    private final PunishmentRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<Punishment> active(Long userId, PunishmentType type) {
        return repo.findActive(userId, type, LocalDateTime.now()).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBanned(Long userId) {
        return active(userId, PunishmentType.BAN).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> muteUntil(String username) {
        return repo.findActiveByUsername(username, PunishmentType.MUTE, LocalDateTime.now())
                .stream().findFirst().map(Punishment::getExpiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> activeUserIds(PunishmentType type) {
        return new HashSet<>(repo.findActiveUserIds(type, LocalDateTime.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Punishment> history(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Новое наказание заменяет действующее наказание того же типа.
     */
    @Override
    @Transactional
    public void issue(User user, PunishmentType type, Duration duration, String term, String reason, User admin) {
        String cleanReason = reason == null || reason.isBlank() ? null : reason.trim();
        if (cleanReason != null && cleanReason.length() > 500) {
            throw new IllegalArgumentException("Причина: до 500 символов");
        }
        LocalDateTime now = LocalDateTime.now();
        repo.findActive(user.getId(), type, now).forEach(p -> p.revoke(admin, now));
        repo.save(new Punishment(user, type, term, cleanReason, now, now.plus(duration), admin));
    }

    @Override
    @Transactional
    public void revoke(Long userId, Long punishmentId, User admin) {
        Punishment p = repo.findById(punishmentId)
                .filter(x -> x.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Наказание не найдено"));
        if (!p.isActive()) throw new IllegalArgumentException("Наказание уже не действует");
        p.revoke(admin, LocalDateTime.now());
    }
}
