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

  public SessionResponseDTO(String sessionId) {
    this.sessionId = sessionId;
  }
}
