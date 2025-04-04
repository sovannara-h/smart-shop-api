package com.ecommerce.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private T data;
  private String message;
  private String error;
  private LocalDateTime timestamp = LocalDateTime.now();
}
