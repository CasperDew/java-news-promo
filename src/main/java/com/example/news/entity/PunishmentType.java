package com.example.news.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PunishmentType {
    MUTE("Мут"),
    BAN("Бан");

    private final String label;
}
