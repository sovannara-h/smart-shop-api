package com.ecommerce.service.interfaces;

import java.time.LocalDateTime;

public interface EditSessionService {
    String startCreateSession(String entityType);
    String startSession(String entityType, Long entityId);
    void confirmSession(String sessionId);
    void cancelSession(String sessionId);
    boolean isSessionValid(String sessionId);
    void registerEntityCreation(String sessionId, String entityType, Long entityId);
    void registerEntityModification(String sessionId, String entityType, Long entityId, Object originalEntity);
    void cleanupExpiredSessions();
    LocalDateTime getSessionExpiryTime(String sessionId);
}