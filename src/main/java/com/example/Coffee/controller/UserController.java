package com.example.Coffee.controller;

import com.example.Coffee.dto.ApiResponse;
import com.example.Coffee.model.User;
import com.example.Coffee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register (@RequestParam String username, @RequestParam String password) {
        try {
            User user = userService.register(username, password);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đăng ký thành công!", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestParam String username, @RequestParam String password) {
        Optional<User> user = userService.login(username, password);
        return user.map(value -> ResponseEntity.ok(new ApiResponse<>(true, "Đăng nhập thành công!", value))).orElseGet(() -> ResponseEntity.status(401).body(new ApiResponse<>(false, "Tài khoản hoặc mật khẩu không chính xác!", null)));
    }
}
