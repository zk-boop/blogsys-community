package com.zk.projectboot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@Component
public class UserStatusValidationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserStatusValidationFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof BlogUserPrincipal) {
            BlogUserPrincipal principal = (BlogUserPrincipal) authentication.getPrincipal();
            Optional<User> current = userRepository.findById(principal.getId());
            String principalRole = principal.getAuthorities().iterator().next().getAuthority();
            boolean valid = current.isPresent()
                    && current.get().getStatus() == User.UserStatus.active
                    && current.get().getRole() != null
                    && principalRole.equals(current.get().getRole().getRoleName());
            if (!valid) {
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                objectMapper.writeValue(response.getWriter(), ApiResponse.fail("账号状态或权限已变更，请重新登录"));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
