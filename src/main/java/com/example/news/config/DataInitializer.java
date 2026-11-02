package com.example.news.config;

import com.example.news.entity.Role;
import com.example.news.entity.User;
import com.example.news.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(UserRepository users, PasswordEncoder encoder, JdbcTemplate jdbc,
                           @Value("${app.admin.username}") String adminUsername,
                           @Value("${app.admin.password}") String adminPassword) {
        this.users = users;
        this.encoder = encoder;
        this.jdbc = jdbc;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        migrateLegacyRoleColumn();
        User admin = users.findByUsername(adminUsername).orElse(null);
        if (admin == null) {
            users.save(new User(adminUsername, adminUsername + "@example.com",
                    encoder.encode(adminPassword), Role.ADMIN));
        } else if (!admin.hasRole(Role.ADMIN)) {
            admin.getRoles().add(Role.ADMIN);
            users.save(admin);
        }
    }

    private void migrateLegacyRoleColumn() {
        Integer n = jdbc.queryForObject("select count(*) from information_schema.columns "
                        + "where table_schema = current_schema() and table_name = 'users' and column_name = 'role'",
                Integer.class);
        if (n == null || n == 0) return;
        jdbc.update("insert into user_roles(user_id, role) select u.id, u.role from users u "
                + "where u.role is not null and not exists "
                + "(select 1 from user_roles r where r.user_id = u.id and r.role = u.role)");
        jdbc.execute("alter table users drop column role");
    }
}
