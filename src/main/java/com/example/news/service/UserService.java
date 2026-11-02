package com.example.news.service;

import com.example.news.dto.RegisterForm;
import com.example.news.entity.Role;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    /** Дополнительные проверки, которые нельзя выразить аннотациями. */
    public void validate(RegisterForm form, BindingResult errors) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        }
        if (form.getUsername() != null && users.existsByUsernameIgnoreCase(form.getUsername())) {
            errors.rejectValue("username", "taken", "Этот логин уже занят");
        }
        if (form.getEmail() != null && users.existsByEmailIgnoreCase(form.getEmail())) {
            errors.rejectValue("email", "taken", "Этот email уже зарегистрирован");
        }
    }

    @Transactional
    public void register(RegisterForm form) {
        users.save(new User(form.getUsername(), form.getEmail().toLowerCase(),
                encoder.encode(form.getPassword()), Role.USER));
    }
}
