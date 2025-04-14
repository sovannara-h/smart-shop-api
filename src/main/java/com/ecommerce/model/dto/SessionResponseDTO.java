package com.ecommerce.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponseDTO {
  private String sessionId;
  private String entityType;
  private Long entityId;
  private LocalDateTime expiresAt;
  private boolean success;
  private String message;

  public SessionResponseDTO(String sessionId) {
    this.sessionId = sessionId;
    this.success = true;
  }

  public SessionResponseDTO(String sessionId, boolean success, String message) {
    this.sessionId = sessionId;
    this.success = success;
    this.message = message;
  }
}
