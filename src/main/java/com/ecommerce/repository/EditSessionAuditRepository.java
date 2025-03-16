package com.ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.model.entity.EditSessionAudit;

@Repository
public interface EditSessionAuditRepository extends JpaRepository<EditSessionAudit, Long> {
    List<EditSessionAudit> findBySessionId(String sessionId);    
    List<EditSessionAudit> findByExpiresAtBefore(LocalDateTime dateTime);
    
    @Transactional
    void deleteBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);
    
    List<EditSessionAudit> findBySessionIdAndEntityTypeAndEntityId(
        String sessionId, String entityType, Long entityId);
}