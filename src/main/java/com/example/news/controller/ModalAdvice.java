package com.example.news.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@ControllerAdvice
public class ModalAdvice {

    private static final Set<String> MODALS = Set.of("login", "register");

    @ModelAttribute
    public void openModal(Model model, @RequestParam(value = "modal", required = false) String modal) {
        if (!model.containsAttribute("openModal") && modal != null && MODALS.contains(modal)) {
            model.addAttribute("openModal", modal);
        }
    }
}
