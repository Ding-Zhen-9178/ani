package com.ani.anitrack.service;

import com.ani.anitrack.entity.User;
import com.ani.anitrack.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("密码至少4位");
        }
        if (userMapper.countByUsername(username) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(encoder.encode(password));
        userMapper.insert(user);
        return user;
    }

    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }
}