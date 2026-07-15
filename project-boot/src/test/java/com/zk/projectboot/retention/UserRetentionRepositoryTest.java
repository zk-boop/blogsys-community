package com.zk.projectboot.retention;

import com.zk.projectboot.model.Role;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.RoleRepository;
import com.zk.projectboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class UserRetentionRepositoryTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private RoleRepository roles;

    @Test
    void sevenDayBoundaryIncludesExactCutoffAndExcludesPermanentAccounts() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 15, 8, 0);
        LocalDateTime cutoff = now.minusDays(7);
        Role role = new Role();
        role.setRoleName("ROLE_TEST");
        role = roles.saveAndFlush(role);

        User recent = persist("recent", User.RetentionPolicy.EPHEMERAL, now.minusDays(6).minusHours(23).minusMinutes(59), role);
        User exact = persist("exact", User.RetentionPolicy.EPHEMERAL, cutoff, role);
        User older = persist("older", User.RetentionPolicy.EPHEMERAL, cutoff.minusMinutes(1), role);
        User permanent = persist("permanent", User.RetentionPolicy.PERMANENT, now.minusDays(30), role);

        List<Integer> expiredIds = users.findByRetentionPolicyAndCreatedAtLessThanEqual(
                        User.RetentionPolicy.EPHEMERAL, cutoff).stream()
                .map(User::getId)
                .collect(Collectors.toList());

        assertFalse(expiredIds.contains(recent.getId()));
        assertTrue(expiredIds.contains(exact.getId()));
        assertTrue(expiredIds.contains(older.getId()));
        assertFalse(expiredIds.contains(permanent.getId()));
    }

    private User persist(String username, User.RetentionPolicy policy, LocalDateTime createdAt, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword("hash");
        user.setRole(role);
        user.setStatus(User.UserStatus.active);
        user.setRetentionPolicy(policy);
        user = users.saveAndFlush(user);
        user.setCreatedAt(createdAt);
        return users.saveAndFlush(user);
    }
}
