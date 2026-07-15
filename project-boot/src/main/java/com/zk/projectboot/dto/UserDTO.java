package com.zk.projectboot.dto;

import com.zk.projectboot.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer roleId;
    private String roleName;
    private User.UserStatus status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setBio(user.getBio());

        if (user.getRole() != null) {
            userDTO.setRoleId(user.getRole().getId());
            userDTO.setRoleName(user.getRole().getRoleName());
        }

        userDTO.setStatus(user.getStatus());
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setCreatedAt(user.getCreatedAt());

        return userDTO;
    }
}
