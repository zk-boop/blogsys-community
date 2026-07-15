package com.zk.projectboot.config;

import com.zk.projectboot.model.Role;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.RoleRepository;
import com.zk.projectboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DemoDataInitializerTest {

    @Test
    void initializerIsOptInAndCreatesIdempotentPermanentAccounts() throws Exception {
        ConditionalOnProperty condition = DemoDataInitializer.class.getAnnotation(ConditionalOnProperty.class);
        assertFalse(condition.matchIfMissing());

        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Role adminRole = role("ROLE_ADMIN");
        Role userRole = role("ROLE_USER");
        when(roles.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roles.findByRoleName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(users.existsByUsername("admin")).thenReturn(false, true);
        when(users.existsByUsername("demo")).thenReturn(false, true);
        when(encoder.encode("admin-secret")).thenReturn("encoded-admin");
        when(encoder.encode("demo-secret")).thenReturn("encoded-demo");

        DemoDataInitializer initializer = new DemoDataInitializer(
                users, roles, encoder,
                "admin", "admin@example.test", "admin-secret",
                "demo", "demo@example.test", "demo-secret");

        initializer.run();
        initializer.run();

        ArgumentCaptor<User> accounts = ArgumentCaptor.forClass(User.class);
        verify(users, times(2)).save(accounts.capture());
        List<User> saved = accounts.getAllValues();
        assertEquals(User.RetentionPolicy.PERMANENT, saved.get(0).getRetentionPolicy());
        assertEquals(User.RetentionPolicy.PERMANENT, saved.get(1).getRetentionPolicy());
        assertNotEquals("admin-secret", saved.get(0).getPassword());
        assertNotEquals("demo-secret", saved.get(1).getPassword());
        verify(encoder).encode("admin-secret");
        verify(encoder).encode("demo-secret");
    }

    private Role role(String name) {
        Role role = new Role();
        role.setRoleName(name);
        return role;
    }
}
