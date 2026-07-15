package com.zk.projectboot.service;

import com.zk.projectboot.config.TestConfig;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetUserById() {
        // 获取ID为1的管理员用户
        Optional<User> foundUser = userService.getUserById(1);

        // 验证结果
        assertTrue(foundUser.isPresent());
        assertEquals("admin", foundUser.get().getUsername());
        assertEquals("admin@blog.com", foundUser.get().getEmail());
        assertEquals(1, foundUser.get().getRole().getId());
    }

    @Test
    void testGetUserByUsername() {
        // 获取用户名为lisi的用户
        Optional<User> foundUser = userService.getUserByUsername("lisi");

        // 验证结果
        assertTrue(foundUser.isPresent());
        assertEquals(3, foundUser.get().getId());
        assertEquals("lisi@blog.com", foundUser.get().getEmail());
    }

    @Test
    void testGetAllUsers() {
        // 获取第一页用户，每页10条
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = userService.getAllUsers(pageable);

        // 验证结果
        assertFalse(userPage.isEmpty());
        assertTrue(userPage.getTotalElements() >= 3); // 至少有3个用户
    }

    @Test
    @Transactional
    void testBanAndActivateUser() {
        // 测试封禁ID为4的用户(wangwu)
        boolean banResult = userService.banUser(4);

        // 验证封禁结果
        assertTrue(banResult);

        // 重新获取用户信息验证状态
        Optional<User> bannedUser = userService.getUserById(4);
        assertTrue(bannedUser.isPresent());
        assertEquals(User.UserStatus.banned, bannedUser.get().getStatus());

        // 测试激活用户
        boolean activateResult = userService.activateUser(4);

        // 验证激活结果
        assertTrue(activateResult);

        // 重新获取用户信息验证状态
        Optional<User> activatedUser = userService.getUserById(4);
        assertTrue(activatedUser.isPresent());
        assertEquals(User.UserStatus.active, activatedUser.get().getStatus());
    }

    @Test
    void testExistsByUsername() {
        // 测试已存在的用户名
        boolean existsAdmin = userService.existsByUsername("admin");
        assertTrue(existsAdmin);

        // 测试不存在的用户名
        boolean existsNonExisting = userService.existsByUsername("nonexistinguser");
        assertFalse(existsNonExisting);
    }
}
