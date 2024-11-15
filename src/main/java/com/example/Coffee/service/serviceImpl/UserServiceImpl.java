package com.example.Coffee.service.serviceImpl;

import com.example.Coffee.repository.UserRepository;
import com.example.Coffee.model.User;
import com.example.Coffee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User register(String username, String password) {
//        kiem tra username da ton tai hay chua
        if(userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên tài khoản đã tồn tại!");
        }

        User user = new User(username, password);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> login(String username, String password) {
       Optional<User> user = userRepository.findByUsername(username);
       if(user.isPresent()) {
           if(user.get().getPassword().equals(password)) {
               return user;
           }
       }
       return Optional.empty();
    }

    @Override
    public User updateUser(Long userId, String fullName, String phoneNumber, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Cập nhật thông tin người dùng
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

}
