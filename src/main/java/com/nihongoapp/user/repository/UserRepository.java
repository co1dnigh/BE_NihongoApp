package com.nihongoapp.user.repository;

import com.nihongoapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.hearts = LEAST(5, u.hearts + :delta) WHERE u.id = :userId")
    int addHearts(Long userId, int delta);
}
