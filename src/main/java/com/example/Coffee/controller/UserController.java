package com.example.Coffee.controller;

import com.example.Coffee.dto.ApiResponse;
import com.example.Coffee.dto.UserDto;
import com.example.Coffee.model.User;
import com.example.Coffee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register (@RequestParam String username, @RequestParam String password) {
        try {
            User user = userService.register(username, password);
            UserDto userDto = new UserDto(user); // Chuyển đổi sang UserDTO
            return ResponseEntity.ok(new ApiResponse<>(true, "Đăng ký thành công!", userDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestParam String username, @RequestParam String password) {
        Optional<User> user = userService.login(username, password);
        return user.map(value -> {
            UserDto userDto = new UserDto(value); // Chuyển đổi sang UserDTO
            return ResponseEntity.ok(new ApiResponse<>(true, "Đăng nhập thành công!", userDto));
        }).orElseGet(() -> ResponseEntity.status(401).body(new ApiResponse<>(false, "Tài khoản hoặc mật khẩu không chính xác!", null)));
    }

    // API cập nhật thông tin người dùng
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @RequestParam Long userId,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            @RequestParam String address) {

        try {
            User updatedUser = userService.updateUser(userId, fullName, phoneNumber, address);
            UserDto userDto = new UserDto(updatedUser); // Chuyển đổi sang UserDTO
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật thông tin thành công!", userDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@RequestParam Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            UserDto userDto = new UserDto(user); // Chuyển đổi sang UserDTO
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin thành công!", userDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
