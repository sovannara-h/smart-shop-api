package com.ecommerce.service.interfaces;

import java.time.LocalDateTime;

/** Service for managing entity edit sessions */
public interface EditSessionService {
  /**
   * Starts a new session for creating an entity
   *
   * @param entityType type of entity being created
   * @return session identifier
   */
  String startCreateSession(String entityType);

  /**
   * Starts a new session for editing an existing entity
   *
   * @param entityType type of entity being edited
   * @param entityId entity identifier
   * @return session identifier
   */
  String startSession(String entityType, Long entityId);

  /**
   * Confirms and finalizes an edit session
   *
   * @param sessionId session identifier
   */
  void confirmSession(String sessionId);

  /**
   * Cancels an edit session and reverts changes
   *
   * @param sessionId session identifier
   */
  void cancelSession(String sessionId);

  /**
   * Checks if a session is valid and not expired
   *
   * @param sessionId session identifier
   * @return true if the session is valid
   */
  boolean isSessionValid(String sessionId);

  /**
   * Registers the creation of an entity within a session
   *
   * @param sessionId session identifier
   * @param entityType type of entity created
   * @param entityId entity identifier
   */
  void registerEntityCreation(String sessionId, String entityType, Long entityId);

  /**
   * Registers the modification of an entity within a session
   *
   * @param sessionId session identifier
   * @param entityType type of entity modified
   * @param entityId entity identifier
   * @param originalEntity original state of the entity
   */
  void registerEntityModification(
      String sessionId, String entityType, Long entityId, Object originalEntity);

  /** Cleans up expired sessions */
  void cleanupExpiredSessions();

  /**
   * Gets the expiry time of a session
   *
   * @param sessionId session identifier
   * @return expiry date and time
   * @throws SessionNotFoundException if the session does not exist
   * @throws IllegalArgumentException if the session identifier is null or empty
   */
  LocalDateTime getExpiryTime(String sessionId);
}
