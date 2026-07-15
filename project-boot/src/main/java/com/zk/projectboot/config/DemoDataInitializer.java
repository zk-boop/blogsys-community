package com.zk.projectboot.config;

import com.zk.projectboot.model.Role;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.RoleRepository;
import com.zk.projectboot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "demo.seed.enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;
    private final String demoUsername;
    private final String demoEmail;
    private final String demoPassword;

    public DemoDataInitializer(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${demo.admin.username:${DEMO_ADMIN_USERNAME:admin}}") String adminUsername,
                               @Value("${demo.admin.email:${DEMO_ADMIN_EMAIL:admin@example.invalid}}") String adminEmail,
                               @Value("${demo.admin.password:${DEMO_ADMIN_PASSWORD:}}") String adminPassword,
                               @Value("${demo.user.username:${DEMO_USER_USERNAME:demo}}") String demoUsername,
                               @Value("${demo.user.email:${DEMO_USER_EMAIL:demo@example.invalid}}") String demoEmail,
                               @Value("${demo.user.password:${DEMO_USER_PASSWORD:}}") String demoPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.demoUsername = demoUsername;
        this.demoEmail = demoEmail;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createAccountIfConfigured(adminUsername, adminEmail, adminPassword, "ROLE_ADMIN", "系统管理员");
        createAccountIfConfigured(demoUsername, demoEmail, demoPassword, "ROLE_USER", "演示用户");
    }

    private void createAccountIfConfigured(String username, String email, String password,
                                           String roleName, String nickname) {
        if (password == null || password.trim().isEmpty()) {
            log.warn("Demo seed is enabled, but no password was configured for {}", username);
            return;
        }
        if (userRepository.existsByUsername(username)) {
            return;
        }

        Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> createRole(roleName));
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(User.UserStatus.active);
        user.setRetentionPolicy(User.RetentionPolicy.PERMANENT);
        userRepository.save(user);
    }

    private Role createRole(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleDescription("ROLE_ADMIN".equals(roleName) ? "系统管理员" : "普通用户");
        role.setPermissions("ROLE_ADMIN".equals(roleName) ? "[\"all\"]" : "[\"comment\",\"like\",\"favorite\"]");
        return roleRepository.save(role);
    }
}
