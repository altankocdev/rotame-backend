package com.altankoc.rotame.user.repository;

import com.altankoc.rotame.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByRefreshToken(String refreshToken);
    int deleteByDeletedTrueAndUpdatedAtBefore(LocalDateTime date);
}
