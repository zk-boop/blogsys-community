package com.zk.projectboot.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.security.BlogUserPrincipal;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LIMIT_MESSAGE = "操作过于频繁，请稍后重试";

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateLimitPolicy policy = resolvePolicy(request.getMethod(), request.getRequestURI());
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String subject = resolveSubject(policy, request);
        if (subject == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = policy.getKeyPrefix() + ":" + subject;
        if (rateLimitService.tryAcquire(key, policy.getLimit(), policy.getWindow())) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(LIMIT_MESSAGE));
    }

    private RateLimitPolicy resolvePolicy(String method, String path) {
        if (HttpMethod.POST.matches(method) && "/api/auth/register".equals(path)) {
            return RateLimitPolicy.REGISTER_IP;
        }
        if (HttpMethod.POST.matches(method) && "/api/auth/login".equals(path)) {
            return RateLimitPolicy.LOGIN_IP;
        }
        if (HttpMethod.POST.matches(method) && "/api/articles".equals(path)) {
            return RateLimitPolicy.CREATE_ARTICLE_USER;
        }
        if (HttpMethod.PATCH.matches(method) && path.matches("^/api/articles/[^/]+/submit$")) {
            return RateLimitPolicy.SUBMIT_ARTICLE_USER;
        }
        if (HttpMethod.POST.matches(method) && "/api/comments".equals(path)) {
            return RateLimitPolicy.COMMENT_USER;
        }
        if (HttpMethod.POST.matches(method) && path.startsWith("/api/files/upload/")) {
            return RateLimitPolicy.UPLOAD_USER;
        }
        return null;
    }

    private String resolveSubject(RateLimitPolicy policy, HttpServletRequest request) {
        if (policy.getSubject() == RateLimitPolicy.Subject.IP) {
            return request.getRemoteAddr();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof BlogUserPrincipal)) {
            return null;
        }
        BlogUserPrincipal principal = (BlogUserPrincipal) authentication.getPrincipal();
        return String.valueOf(principal.getId());
    }
}
