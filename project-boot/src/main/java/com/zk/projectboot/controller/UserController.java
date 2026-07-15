package com.zk.projectboot.controller;

import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.ChangePasswordRequest;
import com.zk.projectboot.dto.UpdateProfileRequest;
import com.zk.projectboot.dto.UserDTO;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.User;
import com.zk.projectboot.security.CurrentUserService;
import com.zk.projectboot.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        return ResponseEntity.ok(ApiResponse.success(UserDTO.fromUser(user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.getAllUsers(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<UserDTO> users = userPage.getContent().stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.searchUsers(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<UserDTO> users = userPage.getContent().stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateProfileRequest request) {
        currentUserService.requireSelfOrAdmin(id);
        User user = userService.getUserById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setBio(request.getBio());
        return ResponseEntity.ok(ApiResponse.success(UserDTO.fromUser(userService.updateUser(user))));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) ChangePasswordRequest body,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String newPassword) {
        currentUserService.requireSelf(id);

        String currentPassword = body == null ? oldPassword : body.getOldPassword();
        String replacementPassword = body == null ? newPassword : body.getNewPassword();
        if (currentPassword == null || currentPassword.trim().isEmpty()
                || replacementPassword == null || replacementPassword.length() < 6
                || replacementPassword.length() > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码参数不符合要求");
        }

        User user = userService.getUserById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!userService.validatePassword(user, currentPassword)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }
        if (!userService.updatePassword(user, replacementPassword)) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "密码更新失败");
        }
        return ResponseEntity.ok(ApiResponse.success("密码更新成功", null));
    }
}
