package com.example.news.repository;

import com.example.news.entity.Punishment;
import com.example.news.entity.PunishmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PunishmentRepository extends JpaRepository<Punishment, Long> {
    List<Punishment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select p from Punishment p where p.user.id = :userId and p.type = :type "
         + "and p.revokedAt is null and p.expiresAt > :now order by p.expiresAt desc")
    List<Punishment> findActive(@Param("userId") Long userId, @Param("type") PunishmentType type,
                                @Param("now") LocalDateTime now);

    @Query("select p from Punishment p where p.user.username = :username and p.type = :type "
         + "and p.revokedAt is null and p.expiresAt > :now order by p.expiresAt desc")
    List<Punishment> findActiveByUsername(@Param("username") String username, @Param("type") PunishmentType type,
                                          @Param("now") LocalDateTime now);

    @Query("select distinct p.user.id from Punishment p where p.type = :type "
         + "and p.revokedAt is null and p.expiresAt > :now")
    List<Long> findActiveUserIds(@Param("type") PunishmentType type, @Param("now") LocalDateTime now);
}
