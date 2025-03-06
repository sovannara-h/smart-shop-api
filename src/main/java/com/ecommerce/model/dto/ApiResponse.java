package com.ecommerce.model.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String error;
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }
}