package com.collide.backend.repository;

import com.collide.backend.model.entity.AppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    Optional<AppUser> findByEmailIgnoreCase(String email);
}
