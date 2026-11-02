package com.example.news.controller;

import com.example.news.dto.PostForm;
import com.example.news.dto.UserCard;
import com.example.news.entity.Role;
import com.example.news.service.AdminUserService;
import com.example.news.service.CommentService;
import com.example.news.service.ImageStorageService;
import com.example.news.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Set<String> STATUSES = Set.of("all", "approved", "hidden");

    private final PostService postService;
    private final CommentService commentService;
    private final ImageStorageService images;
    private final AdminUserService adminUsers;

    @GetMapping
    public String root() {
        return "redirect:/admin/posts";
    }

    // ---------- Посты ----------

    @GetMapping("/posts")
    public String posts(Model model) {
        log.debug("GET /admin/posts - открыт список постов");
        model.addAttribute("postForm", new PostForm());
        return postsPage(model, "url", null);
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm form, BindingResult errors,
                         @RequestParam(defaultValue = "url") String imageSource,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Model model, Principal principal, RedirectAttributes flash) {

        log.info("POST /admin/posts - {} создаёт пост, источник картинки: {}",
                principal.getName(), imageSource);

        boolean fromFile = "file".equals(imageSource);
        if (fromFile && (imageFile == null || imageFile.isEmpty())) {
            log.warn("Создание поста: выбран источник 'file', но файл не передан");
            return postsPage(model, imageSource, "Выберите файл картинки");
        }
        if (errors.hasErrors()) {
            log.warn("Создание поста: ошибки валидации {}", errors.getAllErrors());
            return postsPage(model, imageSource, null);
        }
        String stored = null;
        if (fromFile) {
            try {
                log.info("Картинка загружена: {} ({} байт)", stored, imageFile.getSize());
                stored = images.store(imageFile);
            } catch (IllegalArgumentException e) {
                log.warn("Загрузка картинки отклонена: {}", e.getMessage());
                return postsPage(model, imageSource, e.getMessage());
            }
            form.setImageUrl(stored);
        }
        try {
            postService.create(form, principal.getName());
        } catch (RuntimeException e) {
            images.deleteIfLocal(stored);
            throw e;
        }
        flash.addFlashAttribute("message", "Пост сохранён");
        log.info("Пост '{}' опубликован пользователем {}", form.getTitle(), principal.getName());
        return "redirect:/admin/posts";
    }

    private String postsPage(Model model, String imageSource, String imageError) {
        model.addAttribute("posts", postService.allForAdmin());
        model.addAttribute("imageSource", imageSource);
        model.addAttribute("imageError", imageError);
        return "admin/posts";
    }

    @PostMapping("/posts/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        postService.togglePublished(id);
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes flash) {
        log.info("POST /posts/{}/delete - Удаление поста Администратором", id);

        images.deleteIfLocal(postService.delete(id));
        flash.addFlashAttribute("message", "Пост удалён");
        return "redirect:/admin/posts";
    }

    // ---------- Комментарии ----------

    @GetMapping("/comments")
    public String comments(@RequestParam(defaultValue = "all") String status, Model model) {
        if (!STATUSES.contains(status)) status = "all";
        model.addAttribute("status", status);
        model.addAttribute("comments", commentService.forModeration(status));

        log.debug("Администрацией получен список комментариев. Статус: {}", status);
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(defaultValue = "all") String status) {
        commentService.setApproved(id, true);
        log.info("Комментарий {} успешно одобрен", id);
        return back(status);
    }

    @PostMapping("/comments/{id}/hide")
    public String hide(@PathVariable Long id, @RequestParam(defaultValue = "all") String status) {
        commentService.setApproved(id, false);
        log.info("Комментарий {} скрыт", id);
        return back(status);
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, @RequestParam(defaultValue = "all") String status) {
        log.debug("Удаление комментария {}", id);
        commentService.delete(id);
        return back(status);
    }

    private static String back(String status) {
        return "redirect:/admin/comments?status=" + (STATUSES.contains(status) ? status : "all");
    }

    // ---------- Пользователи ----------

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Long open, Model model) {

        log.debug("Администратором запрошен списко пользователей");

        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("users", adminUsers.search(q));
        model.addAttribute("openUser", open);
        return "admin/users";
    }

    /**
     * HTML мини-окна пользователя, его подгружает admin.js.
     */
    @GetMapping("/users/{id}/card")
    public String card(@PathVariable Long id, @RequestParam(defaultValue = "") String q,
                       Model model, Principal principal) {
        log.debug("Получение карточки пользователя с ID: {}", id);

        UserCard card = adminUsers.findCard(id, principal.getName())
                .orElseThrow(() -> {
                    log.warn("Карточка пользователя ID={} не найдена или недоступна", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });

        model.addAttribute("card", card);
        model.addAttribute("q", q);
        return "admin/user-card :: card";
    }

    @PostMapping("/users/{id}/roles/add")
    public String addRole(@PathVariable Long id, @RequestParam Role role,
                          @RequestParam(defaultValue = "") String q, RedirectAttributes ra) {
        log.debug("Добавление роли {} для пользователя {}", role, id);
        return userAction(id, q, ra, "Роль добавлена", () -> adminUsers.addRole(id, role));
    }

    @PostMapping("/users/{id}/roles/remove")
    public String removeRole(@PathVariable Long id, @RequestParam Role role,
                             @RequestParam(defaultValue = "") String q, Principal principal, RedirectAttributes ra) {
        log.debug("Удаление роли {} у пользователя {}", role, id);
        return userAction(id, q, ra, "Роль удалена", () -> adminUsers.removeRole(id, role, principal.getName()));
    }

    @PostMapping("/users/{id}/mute")
    public String mute(@PathVariable Long id, @RequestParam(required = false) Integer amount,
                       @RequestParam String unit, @RequestParam(required = false) String reason,
                       @RequestParam(defaultValue = "") String q, Principal principal, RedirectAttributes ra) {

        log.debug("Выдача наказания 'Мут' пользователю ID={} Срок: {} {}, администратор: {}",
                id, amount, unit, principal.getName());


        return userAction(id, q, ra, "Мут выдан", () -> {
            if (amount == null) throw new IllegalArgumentException("Укажите срок мута");
            adminUsers.mute(id, amount, unit, reason, principal.getName());
        });
    }

    @PostMapping("/users/{id}/ban")
    public String ban(@PathVariable Long id, @RequestParam(required = false) Integer days,
                      @RequestParam(required = false) String reason,
                      @RequestParam(defaultValue = "") String q, Principal principal, RedirectAttributes ra) {

        log.debug("Выдача наказания 'Бан' пользователю ID={}. Дней: {}, администратор: {}",
                id, days, principal.getName());

        return userAction(id, q, ra, "Бан выдан", () -> {
            if (days == null) throw new IllegalArgumentException("Укажите срок бана в днях");
            adminUsers.ban(id, days, reason, principal.getName());
        });
    }

    @PostMapping("/users/{id}/punishments/{pid}/revoke")
    public String revoke(@PathVariable Long id, @PathVariable Long pid,
                         @RequestParam(defaultValue = "") String q, Principal principal, RedirectAttributes ra) {

        log.debug("Снятие наказания ID={} у пользователя ID={}, администратором {}", pid, id, principal.getName());

        return userAction(id, q, ra, "Наказание снято", () -> adminUsers.revoke(id, pid, principal.getName()));
    }

    /**
     * Выполняет действие и возвращает админа к списку с открытым окном того же пользователя.
     */
    private static String userAction(Long id, String q, RedirectAttributes ra, String ok, Runnable action) {
        try {
            action.run();
            ra.addFlashAttribute("message", ok);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        ra.addAttribute("open", id);
        if (!q.isBlank()) ra.addAttribute("q", q);
        return "redirect:/admin/users";
    }
}
