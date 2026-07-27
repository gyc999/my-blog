package com.gyc.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gyc.blog.entity.User;
import com.gyc.blog.entity.vo.UserVO;
import com.gyc.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setNickname("测试用户");
        testUser.setRole(0);
    }

    @Test
    @DisplayName("注册成功 — 用户名不重复")
    void register_shouldSucceed_whenUsernameNotExists() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        boolean result = userService.register(testUser);

        assertTrue(result);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 — 用户名已存在")
    void register_shouldFail_whenUsernameExists() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        boolean result = userService.register(testUser);

        assertFalse(result);
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册时密码应被 BCrypt 加密")
    void register_shouldEncryptPassword() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.register(testUser);

        // 密码应该是 BCrypt 格式 ($2a$...)
        assertTrue(testUser.getPassword().startsWith("$2a$"));
    }

    @Test
    @DisplayName("登录成功 — 用户名密码正确")
    void login_shouldReturnToken_whenCredentialsCorrect() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser.setPassword(encoder.encode("password123"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        String token = userService.login("testuser", "password123");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("登录失败 — 密码错误")
    void login_shouldReturnNull_whenPasswordWrong() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser.setPassword(encoder.encode("password123"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        String token = userService.login("testuser", "wrongpassword");

        assertNull(token);
    }

    @Test
    @DisplayName("登录失败 — 用户不存在")
    void login_shouldReturnNull_whenUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        String token = userService.login("nobody", "password");

        assertNull(token);
    }

    @Test
    @DisplayName("获取用户信息 — 应返回不含密码的 UserVO")
    void getUserById_shouldReturnUserVO_withoutPassword() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        UserVO vo = userService.getUserById(1L);

        assertNotNull(vo);
        assertEquals("testuser", vo.getUsername());
        assertEquals("测试用户", vo.getNickname());
        // UserVO 没有 password 字段,验证编译级别已隔离
    }

    @Test
    @DisplayName("获取用户信息 — 用户不存在返回 null")
    void getUserById_shouldReturnNull_whenUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        UserVO vo = userService.getUserById(999L);

        assertNull(vo);
    }

    @Test
    @DisplayName("更新资料成功")
    void updateProfile_shouldSucceed() {
        User update = new User();
        update.setId(1L);
        update.setNickname("新昵称");
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = userService.updateProfile(update);

        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }
}
