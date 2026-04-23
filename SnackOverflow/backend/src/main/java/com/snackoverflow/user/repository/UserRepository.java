package com.snackoverflow.user.repository;

import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndStatusNot(String username, UserStatus status);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
