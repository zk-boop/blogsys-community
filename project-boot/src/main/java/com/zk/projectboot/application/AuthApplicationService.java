package com.zk.projectboot.application;

import com.zk.projectboot.dto.LoginRequest;
import com.zk.projectboot.dto.RegisterRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Role;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.RoleRepository;
import com.zk.projectboot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthApplicationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthApplicationService(AuthenticationManager authenticationManager,
                                  UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (user.getStatus() == User.UserStatus.banned) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号已被禁用，无法登录");
        }
        if (user.getStatus() != User.UserStatus.active) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号未激活，无法登录");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return new LoginResult(user, authentication);
        } catch (AuthenticationException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名已被使用");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(HttpStatus.CONFLICT, "邮箱已被使用");
        }

        Role role = roleRepository.findByRoleName("ROLE_USER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName("ROLE_USER");
            newRole.setRoleDescription("普通用户");
            return roleRepository.save(newRole);
        });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().trim().isEmpty()
                ? request.getUsername() : request.getNickname().trim());
        user.setAvatar(request.getAvatar());
        user.setRole(role);
        user.setStatus(User.UserStatus.active);
        user.setRetentionPolicy(User.RetentionPolicy.EPHEMERAL);
        return userRepository.save(user);
    }

    public static class LoginResult {
        private final User user;
        private final Authentication authentication;

        public LoginResult(User user, Authentication authentication) {
            this.user = user;
            this.authentication = authentication;
        }

        public User getUser() {
            return user;
        }

        public Authentication getAuthentication() {
            return authentication;
        }
    }
}
