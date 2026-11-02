package com.example.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostForm {
    @NotBlank(message = "Укажите заголовок")
    @Size(max = 200, message = "Заголовок: до 200 символов")
    private String title;

    @NotBlank(message = "Укажите анонс")
    @Size(max = 500, message = "Анонс: до 500 символов")
    private String summary;

    @NotBlank(message = "Напишите текст поста")
    private String content;

    @Size(max = 500, message = "Ссылка слишком длинная")
    @Pattern(regexp = "^$|^https?://.+", message = "Ссылка должна начинаться с http:// или https://")
    private String imageUrl;

    private boolean published = true;

}
