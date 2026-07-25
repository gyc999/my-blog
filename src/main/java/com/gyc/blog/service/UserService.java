package com.gyc.blog.service;

import com.gyc.blog.entity.User;
import com.gyc.blog.entity.vo.UserVO;

public interface UserService {
    boolean register(User user);
    String login(String username, String password);
    UserVO getUserById(Long id);
    boolean updateProfile(User user);
}