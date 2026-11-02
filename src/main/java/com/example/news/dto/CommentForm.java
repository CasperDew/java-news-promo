package com.example.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentForm {
    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(max = 2000, message = "Комментарий: до 2000 символов")
    private String text;

}
