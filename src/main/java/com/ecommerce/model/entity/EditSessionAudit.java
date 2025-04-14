package com.ecommerce.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "edit_session_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditSessionAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  @Index(name = "idx_edit_session_audit_session_id")
  private String sessionId;

  @Column(name = "entity_type", nullable = false)
  @Index(name = "idx_edit_session_audit_entity_type")
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  @Index(name = "idx_edit_session_audit_entity_id")
  private Long entityId;

  @Column(name = "action", nullable = false)
  private String action; // "CREATE", "UPDATE", "DELETE"

  @Column(name = "previous_state", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> previousState;

  @Column(name = "created_at", nullable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "expires_at", nullable = false)
  @Index(name = "idx_edit_session_audit_expires_at")
  private LocalDateTime expiresAt;
}
