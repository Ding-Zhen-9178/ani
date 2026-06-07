package com.ani.anitrack.controller;

import com.ani.anitrack.config.JwtUtil;
import com.ani.anitrack.entity.User;
import com.ani.anitrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(body.get("username"), body.get("password"));
            String token = jwtUtil.generateToken(user.getId());
            return ResponseEntity.ok(Map.of("token", token, "username", user.getUsername(), "createdAt", user.getCreatedAt() != null ? user.getCreatedAt() : ""));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        try {
            User user = userService.login(body.get("username"), body.get("password"));
            String token = jwtUtil.generateToken(user.getId());
            return ResponseEntity.ok(Map.of("token", token, "username", user.getUsername(), "createdAt", user.getCreatedAt() != null ? user.getCreatedAt() : ""));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}