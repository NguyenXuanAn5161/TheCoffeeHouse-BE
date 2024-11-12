package com.example.Coffee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF theo API mới
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll()
//                        .requestMatchers("/api/auth/register").permitAll()  // Cho phép truy cập không cần xác thực
//                        .anyRequest().authenticated()  // Các request khác yêu cầu xác thực
                );

        return http.build();
    }
}