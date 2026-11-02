package com.example.news.controller;

import com.example.news.dto.CommentForm;
import com.example.news.dto.PostView;
import com.example.news.service.CommentService;
import com.example.news.service.MutedException;
import com.example.news.service.PostService;
import com.example.news.service.PunishmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final PostService postService;
    private final CommentService commentService;
    private final PunishmentService punishments;

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model, Principal principal) {
        log.debug("GET /posts/{} — пользователь: {}", id, principal.getName());
        fill(id, model, principal);
        model.addAttribute("commentForm", new CommentForm());
        return "post";
    }

    @PostMapping("/{id}/comments")
    public String comment(@PathVariable Long id, @Valid @ModelAttribute("commentForm") CommentForm form,
                          BindingResult errors, Model model, Principal principal) {
        log.info("POST /posts/{}/comments — пользователь {} отправляет комментарий", id, principal.getName());
        if (!errors.hasErrors()) {
            try {
                commentService.add(id, principal.getName(), form.getText());
                log.info("Комментарий к посту {} от {} сохранён", id, principal.getName());
                return "redirect:/posts/" + id + "#comments";
            } catch (MutedException e) {
                log.warn("Пользователь {} в муте до {}, комментарий к посту {} отклонён",
                        principal.getName(), e.getUntil(), id);
                errors.rejectValue("text", "muted", "Вам запрещено комментировать до " +
                        e.getUntil().format(FMT));
            }
        }
        fill(id, model, principal);
        return "post";
    }

    private void fill(Long id, Model model, Principal principal) {
        PostView view = postService.published(id, principal == null ? null : principal.getName());
        model.addAttribute("p", view);
        model.addAttribute("comments", commentService.visibleFor(id));
        model.addAttribute("muteUntil", principal == null ? null
                : punishments.muteUntil(principal.getName()).orElse(null));
    }
}
