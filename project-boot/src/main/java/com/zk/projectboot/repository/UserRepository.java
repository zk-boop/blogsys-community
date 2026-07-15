package com.zk.projectboot.repository;

import com.zk.projectboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Page<User> findByUsernameContainingOrNicknameContaining(String username, String nickname, Pageable pageable);
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    List<User> findByRetentionPolicyAndCreatedAtLessThanEqual(
            User.RetentionPolicy retentionPolicy, LocalDateTime cutoff);
}
