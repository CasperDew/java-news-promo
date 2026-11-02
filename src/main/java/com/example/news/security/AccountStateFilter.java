package com.example.news.security;

import com.example.news.repository.UserRepository;
import com.example.news.service.PunishmentService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * На каждом запросе сверяет сессию с базой: забаненного пользователя разлогинивает,
 * а изменённые админом роли применяет сразу, без повторного входа.
 */
@RequiredArgsConstructor
public class AccountStateFilter extends OncePerRequestFilter {
    private final UserRepository users;
    private final PunishmentService punishments;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/uploads/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }
        var user = users.findByUsername(auth.getName()).orElse(null);
        if (user == null || punishments.isBanned(user.getId())) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/?modal=login&banned");
            }
            return;
        }
        Set<String> actual = user.getRoles().stream().map(r -> "ROLE_" + r.name()).collect(Collectors.toSet());
        Set<String> current = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        if (!actual.equals(current)) {
            var updated = UsernamePasswordAuthenticationToken.authenticated(auth.getPrincipal(), null,
                    actual.stream().map(SimpleGrantedAuthority::new).toList());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(updated);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);
        }
        chain.doFilter(request, response);
    }
}
