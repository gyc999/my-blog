package com.gyc.blog.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gyc.blog.common.JwtUtil;
import com.gyc.blog.entity.User;
import com.gyc.blog.entity.vo.UserVO;
import com.gyc.blog.mapper.UserMapper;
import com.gyc.blog.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(User user) {
        // 1. 检查用户名是否重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return false;
        }
        // 2. 密码加密
        user.setPassword(encoder.encode(user.getPassword()));
        // 3. 默认角色
        if (user.getRole() == null) user.setRole(0);
        // 4. 入库
        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) return null;

        if (encoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(user.getId(), user.getUsername());
        }
        return null;
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return null;
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public boolean updateProfile(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) return false;
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
        return userMapper.updateById(existing) > 0;
    }
}