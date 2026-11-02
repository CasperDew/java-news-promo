package com.example.news.repository;

import com.example.news.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(String username, String email);
}
