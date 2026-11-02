package com.example.news.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {
    @NotBlank(message = "Укажите логин")
    @Size(min = 3, max = 30, message = "Логин: от 3 до 30 символов")
    @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "Логин: латиница, цифры, точка, дефис, подчёркивание")
    private String username;

    @NotBlank(message = "Укажите email")
    @Email(message = "Некорректный email")
    @Size(max = 120, message = "Email слишком длинный")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 8, max = 72, message = "Пароль: от 8 до 72 символов")
    private String password;

    private String confirmPassword;

}
