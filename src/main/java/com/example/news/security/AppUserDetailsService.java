package com.example.news.security;

import com.example.news.repository.UserRepository;
import com.example.news.service.PunishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    private final PunishmentService punishments;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .accountLocked(punishments.isBanned(u.getId()))
                .build();
    }
}
