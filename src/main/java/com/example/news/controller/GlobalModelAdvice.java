package com.example.news.controller;

import com.example.news.dto.RegisterForm;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Форма регистрации нужна на каждой странице: модальное окно лежит в общем шаблоне. */
@ControllerAdvice
public class GlobalModelAdvice {
    @ModelAttribute("registerForm")
    public RegisterForm registerForm() {
        return new RegisterForm();
    }
}
