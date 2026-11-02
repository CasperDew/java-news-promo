package com.example.news.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "punishments", indexes = @Index(name = "idx_punishments_user", columnList = "user_id"))
public class Punishment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PunishmentType type;

    /**
     * Срок в том виде, в каком его задал админ, например «3 ч.» или «7 дн.».
     */
    @Column(nullable = false, length = 30)
    private String term;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "issued_by_id")
    private User issuedBy;

    private LocalDateTime revokedAt;

    @ManyToOne
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;

    public Punishment(User user, PunishmentType type, String term, String reason,
                      LocalDateTime createdAt, LocalDateTime expiresAt, User issuedBy) {
        this.user = user;
        this.type = type;
        this.term = term;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.issuedBy = issuedBy;
    }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }

    public String getStatus() {
        if (revokedAt != null) return "Снято";
        return isActive() ? "Действует" : "Истекло";
    }

    public void revoke(User by, LocalDateTime at) {
        this.revokedAt = at;
        this.revokedBy = by;
    }
}
