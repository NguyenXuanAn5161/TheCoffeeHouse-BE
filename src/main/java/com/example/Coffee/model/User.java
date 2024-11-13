package com.example.Coffee.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String fullName; // Họ tên của người dùng

    @Column(unique = true)
    private String phoneNumber; // Số điện thoại

    @Column
    private String address; // Địa chỉ

    // Constructor có tham số
    public User(String username, String password, String fullName, String phoneNumber, String address) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor không tham số
    public User() {}
}
