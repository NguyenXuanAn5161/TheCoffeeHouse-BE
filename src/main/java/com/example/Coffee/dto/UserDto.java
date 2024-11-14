package com.example.Coffee.dto;

import com.example.Coffee.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String address;

    // Constructor để tạo UserDTO từ User
    public UserDto(Optional<User> user) {
        User u = user.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));
        this.id = u.getId();
        this.username = u.getUsername();
        this.fullName = u.getFullName();
        this.phoneNumber = u.getPhoneNumber();
        this.address = u.getAddress();
    }


    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
    }
}