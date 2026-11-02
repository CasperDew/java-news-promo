package com.example.news.controller;

import com.example.news.dto.RegisterForm;
import com.example.news.service.PostService;
import com.example.news.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final PostService postService;
    private final UserService userService;

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("posts", postService.feed(principal == null ? null : principal.getName()));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/?modal=login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/?modal=register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult errors, Model model) {
        userService.validate(form, errors);
        if (errors.hasErrors()) {
            model.addAttribute("posts", postService.feed(null));
            model.addAttribute("openModal", "register");
            return "index";
        }
        userService.register(form);
        return "redirect:/?modal=login&registered";
    }
}
