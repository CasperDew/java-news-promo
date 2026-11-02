package com.example.news.service;

import com.example.news.dto.UserCard;
import com.example.news.dto.UserRow;
import com.example.news.entity.PunishmentType;
import com.example.news.entity.Role;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private static final Duration MAX_MUTE = Duration.ofDays(365);
    private static final int MAX_BAN_DAYS = 3650;

    private final UserRepository users;
    private final PunishmentService punishments;

    @Override
    @Transactional(readOnly = true)
    public List<UserRow> search(String query) {
        List<User> found = query == null || query.isBlank()
                ? users.findAll(Sort.by("username"))
                : users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(query, query);
        Set<Long> muted = punishments.activeUserIds(PunishmentType.MUTE);
        Set<Long> banned = punishments.activeUserIds(PunishmentType.BAN);
        return found.stream()
                .map(u -> new UserRow(u, sorted(u.getRoles()), muted.contains(u.getId()), banned.contains(u.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserCard card(Long id, String adminName) {
        User u = get(id);
        List<Role> missing = Arrays.stream(Role.values()).filter(r -> !u.getRoles().contains(r)).toList();
        return new UserCard(u, sorted(u.getRoles()), missing,
                punishments.active(u.getId(), PunishmentType.MUTE).orElse(null),
                punishments.active(u.getId(), PunishmentType.BAN).orElse(null),
                punishments.history(u.getId()),
                u.getUsername().equals(adminName));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCard> findCard(Long id, String adminName) {
        return users.existsById(id) ? Optional.of(card(id, adminName)) : Optional.empty();
    }

    @Override
    @Transactional
    public void addRole(Long id, Role role) {
        User u = get(id);
        if (!u.getRoles().add(role)) {
            throw new IllegalArgumentException("У пользователя уже есть роль «" + role.getLabel() + "»");
        }
    }

    @Override
    @Transactional
    public void removeRole(Long id, Role role, String adminName) {
        User u = get(id);
        if (!u.getRoles().contains(role)) throw new IllegalArgumentException("У пользователя нет этой роли");
        if (u.getRoles().size() == 1)
            throw new IllegalArgumentException("У пользователя должна остаться хотя бы одна роль");
        if (role == Role.ADMIN && u.getUsername().equals(adminName)) {
            throw new IllegalArgumentException("Нельзя снять роль администратора с самого себя");
        }
        u.getRoles().remove(role);
    }

    @Override
    @Transactional
    public void mute(Long id, int amount, String unit, String reason, String adminName) {
        User target = get(id);
        User admin = admin(adminName);
        notSelf(target, admin);
        if (amount < 1) throw new IllegalArgumentException("Срок должен быть больше нуля");
        Duration duration = switch (unit) {
            case "MINUTES" -> Duration.ofMinutes(amount);
            case "HOURS" -> Duration.ofHours(amount);
            case "DAYS" -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Неизвестная единица срока");
        };
        if (duration.compareTo(MAX_MUTE) > 0) throw new IllegalArgumentException("Мут: не больше 365 дней");
        String label = switch (unit) {
            case "MINUTES" -> "мин.";
            case "HOURS" -> "ч.";
            default -> "дн.";
        };
        punishments.issue(target, PunishmentType.MUTE, duration, amount + " " + label, reason, admin);
    }

    @Override
    @Transactional
    public void ban(Long id, int days, String reason, String adminName) {
        User target = get(id);
        User admin = admin(adminName);
        notSelf(target, admin);
        if (days < 1 || days > MAX_BAN_DAYS)
            throw new IllegalArgumentException("Бан: от 1 до " + MAX_BAN_DAYS + " дней");
        punishments.issue(target, PunishmentType.BAN, Duration.ofDays(days), days + " дн.", reason, admin);
    }

    @Override
    @Transactional
    public void revoke(Long userId, Long punishmentId, String adminName) {
        get(userId);
        punishments.revoke(userId, punishmentId, admin(adminName));
    }

    private User get(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private User admin(String username) {
        return users.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private static void notSelf(User target, User admin) {
        if (target.getId().equals(admin.getId())) throw new IllegalArgumentException("Нельзя наказать самого себя");
    }

    private static List<Role> sorted(Set<Role> roles) {
        return roles.stream().sorted().toList();
    }
}
