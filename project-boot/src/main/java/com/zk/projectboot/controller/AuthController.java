package com.zk.projectboot.controller;

import com.zk.projectboot.application.AuthApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.LoginRequest;
import com.zk.projectboot.dto.RegisterRequest;
import com.zk.projectboot.model.User;
import com.zk.projectboot.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authService;
    private final CurrentUserService currentUserService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(AuthApplicationService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthApplicationService.LoginResult result = authService.login(loginRequest);

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(result.getAuthentication());
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(ApiResponse.success(toUserInfo(result.getUser())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(
                request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(ApiResponse.success("退出登录成功"));
    }

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<Map<String, String>>> csrf(CsrfToken token) {
        Map<String, String> data = new HashMap<>();
        data.put("headerName", token.getHeaderName());
        data.put("parameterName", token.getParameterName());
        data.put("token", token.getToken());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserInfo() {
        Optional<User> user = currentUserService.findCurrentUser();
        if (!user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.fail("用户未登录或信息获取失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(toUserInfo(user.get())));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success("用户注册成功"));
    }

    private Map<String, Object> toUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().getRoleName());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("nickname", user.getNickname() == null ? user.getUsername() : user.getNickname());
        return userInfo;
    }
}
