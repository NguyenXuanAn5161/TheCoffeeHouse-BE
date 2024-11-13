package com.example.Coffee.service;

import com.example.Coffee.model.User;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

public interface UserService {
    User register(String username, String password);
    Optional<User> login(String username, String password);
    User updateUser(Long userId, String fullName, String phoneNumber, String address);
}
