package com.example.Coffee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int totalPages;
    private long totalElements;
    private String author = "Nguyen Xuan An";

    public ApiResponse(boolean success, String message, T data, int totalPages, long totalElements ) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(String message) {
        this.success = false;
        this.message = message;
    }}

