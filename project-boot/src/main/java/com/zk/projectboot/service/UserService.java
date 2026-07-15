package com.zk.projectboot.service;

import com.zk.projectboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    User saveUser(User user);

    Optional<User> getUserById(Integer id);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    Page<User> getAllUsers(Pageable pageable);

    Page<User> searchUsers(String keyword, Pageable pageable);

    Page<User> getUsersByStatus(User.UserStatus status, Pageable pageable);

    User updateUser(User user);

    void deleteUser(Integer id);

    boolean banUser(Integer id);

    boolean activateUser(Integer id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * 验证用户密码是否正确
     * @param user 用户对象
     * @param password 待验证的密码
     * @return 密码是否正确
     */
    boolean validatePassword(User user, String password);

    /**
     * 更新用户密码
     * @param user 用户对象
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    boolean updatePassword(User user, String newPassword);
}
