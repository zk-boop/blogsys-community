package com.zk.projectboot.security;

import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        Optional<User> user;
        if (principal instanceof BlogUserPrincipal) {
            user = userRepository.findById(((BlogUserPrincipal) principal).getId());
        } else {
            user = userRepository.findByUsername(authentication.getName());
        }
        return user.filter(value -> value.getStatus() == User.UserStatus.active);
    }

    public User requireCurrentUser() {
        return findCurrentUser().orElseThrow(() ->
                new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录或账号状态已失效"));
    }

    public void requireSelf(Integer userId) {
        User actor = requireCurrentUser();
        if (!actor.getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "不能操作其他用户的资源");
        }
    }

    public void requireSelfOrAdmin(Integer userId) {
        User actor = requireCurrentUser();
        if (!actor.getId().equals(userId) && !isAdmin(actor)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "权限不足");
        }
    }

    public User requireAdmin() {
        User actor = requireCurrentUser();
        if (!isAdmin(actor)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "仅管理员可以执行此操作");
        }
        return actor;
    }

    public boolean isAdmin(User user) {
        return user.getRole() != null && "ROLE_ADMIN".equals(user.getRole().getRoleName());
    }
}
