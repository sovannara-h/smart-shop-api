package com.ecommerce.service.impl;

import com.ecommerce.exception.EntityLockedException;
import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.exception.SessionNotFoundException;
import com.ecommerce.exception.SessionOperationException;
import com.ecommerce.model.entity.EditSessionAudit;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.EditSessionAuditRepository;
import com.ecommerce.service.interfaces.EditSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the edit session management service. Handles creation, confirmation, and
 * cancellation of edit sessions, as well as tracking changes made during a session.
 */
@Service
@Slf4j
public class EditSessionServiceImpl implements EditSessionService {

  private static final int SESSION_EXPIRY_HOURS = 24;
  private static final String CREATE_ACTION = "CREATE";
  private static final String UPDATE_ACTION = "UPDATE";
  private static final String DELETE_ACTION = "DELETE";
  private static final String CREATE_SESSION_ACTION = "CREATE_SESSION";

  private final Map<String, JpaRepository<?, Long>> repositories;
  private final EditSessionAuditRepository auditRepository;
  private final ObjectMapper objectMapper;
  private final EntityManager entityManager;

  public EditSessionServiceImpl(
      List<JpaRepository<?, Long>> repositoryList,
      EditSessionAuditRepository auditRepository,
      ObjectMapper objectMapper,
      EntityManager entityManager) {
    this.repositories = initializeRepositoryMap(repositoryList);
    this.auditRepository = auditRepository;
    this.objectMapper = objectMapper;
    this.entityManager = entityManager;
  }

  /** Initializes the repository map by mapping entity types to their repositories. */
  private Map<String, JpaRepository<?, Long>> initializeRepositoryMap(
      List<JpaRepository<?, Long>> repositoryList) {
    Map<String, JpaRepository<?, Long>> repoMap = new HashMap<>();

    for (JpaRepository<?, Long> repository : repositoryList) {
      Class<?> entityClass = getEntityClass(repository);
      if (entityClass != null) {
        String entityType = entityClass.getSimpleName().toUpperCase();
        repoMap.put(entityType, repository);
        log.info("Repository registered for entity: {}", entityType);
      }
    }

    return repoMap;
  }

  /**
   * Gets all interfaces implemented by a class, including those inherited from its superclasses and
   * interfaces.
   */
  private Set<Class<?>> getAllInterfaces(Class<?> clazz) {
    Set<Class<?>> interfaces = new HashSet<>();

    for (Class<?> iface : clazz.getInterfaces()) {
      interfaces.add(iface);
      interfaces.addAll(getAllInterfaces(iface));
    }

    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null) {
      interfaces.addAll(getAllInterfaces(superclass));
    }

    return interfaces;
  }

  /** Determines the entity class managed by a given JPA repository using reflection. */
  @SuppressWarnings("unchecked")
  private Class<?> getEntityClass(JpaRepository<?, Long> repository) {
    try {
      Class<?> repositoryClass = repository.getClass();

      for (Class<?> iface : getAllInterfaces(repositoryClass)) {
        if (iface.getGenericInterfaces().length > 0) {
          for (java.lang.reflect.Type type : iface.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
              ParameterizedType paramType = (ParameterizedType) type;
              if (paramType.getRawType().equals(JpaRepository.class)) {
                return (Class<?>) paramType.getActualTypeArguments()[0];
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.warn(
          "Unable to determine entity class for repository: {}", repository.getClass().getName());
    }
    return null;
  }

  /** Removes complex relationships from an entity's state to avoid circular references. */
  private void removeComplexRelations(String entityType, Map<String, Object> entityState) {
    switch (entityType) {
      case "PRODUCT":
        entityState.remove("variants");
        entityState.remove("categories");
        break;
      case "CATEGORY":
        entityState.remove("products");
        entityState.remove("children");
        entityState.remove("parent");
        break;
    }

    entityState.remove("hibernateLazyInitializer");
  }

  /** Determines the processing order of entity types during session operations. */
  private List<String> determineProcessingOrder(Set<String> entityTypes) {
    Map<String, Integer> priorityMap = new HashMap<>();
    priorityMap.put("ATTRIBUTEVALUE", 1);
    priorityMap.put("VARIANT", 2);
    priorityMap.put("PRODUCT", 3);
    priorityMap.put("CATEGORY", 3);
    priorityMap.put("USER", 4);

    return entityTypes.stream()
        .sorted(Comparator.comparingInt(type -> priorityMap.getOrDefault(type, 100)))
        .collect(Collectors.toList());
  }

  /** Converts a value to the specified target type. */
  @SuppressWarnings("unchecked")
  private Object convertValueToType(Object value, Class<?> targetType) {
    if (value == null) return null;
    if (targetType.isAssignableFrom(value.getClass())) return value;

    if (targetType == String.class) {
      return value.toString();
    } else if (targetType == Integer.class || targetType == int.class) {
      return value instanceof Number
          ? ((Number) value).intValue()
          : Integer.parseInt(value.toString());
    } else if (targetType == Long.class || targetType == long.class) {
      return value instanceof Number
          ? ((Number) value).longValue()
          : Long.parseLong(value.toString());
    } else if (targetType == Double.class || targetType == double.class) {
      return value instanceof Number
          ? ((Number) value).doubleValue()
          : Double.parseDouble(value.toString());
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
    } else if (targetType == LocalDateTime.class && value instanceof String) {
      return LocalDateTime.parse((String) value);
    } else if (targetType.isEnum() && value instanceof String) {
      return Enum.valueOf((Class<Enum>) targetType, (String) value);
    }

    // For complex types, use ObjectMapper
    return objectMapper.convertValue(value, targetType);
  }

  /** Restores entity state from a map of property values. */
  @Transactional
  private void restoreEntityFromState(Object entity, Map<String, Object> state) {
    if (state == null) return;

    for (Map.Entry<String, Object> entry : state.entrySet()) {
      String property = entry.getKey();
      Object value = entry.getValue();

      if (property.equals("id") || property.equals("version")) continue;

      applyPropertyToEntity(entity, property, value);
    }

    setEntitySessionId(entity, null, null, false);
  }

  /** Applies a property value to an entity using reflection. */
  private void applyPropertyToEntity(Object entity, String property, Object value) {
    String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);

    try {
      Method[] methods = entity.getClass().getMethods();
      for (Method method : methods) {
        if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
          Class<?> paramType = method.getParameterTypes()[0];
          Object convertedValue = convertValueToType(value, paramType);
          method.invoke(entity, convertedValue);
          break;
        }
      }
    } catch (Exception e) {
      log.debug(
          "Unable to restore property {} on entity {}: {}",
          property,
          entity.getClass().getSimpleName(),
          e.getMessage());
    }
  }

  /** Gets the session ID from an entity using reflection. */
  @Transactional(readOnly = true)
  private String extractSessionIdFromEntity(Object entity) {
    try {
      Method getSessionId = entity.getClass().getMethod("getSessionId");
      String currentSessionId = (String) getSessionId.invoke(entity);
      if (currentSessionId != null) {
        throw new EntityLockedException("Entity is already in edit session in another session");
      }
      return currentSessionId;
    } catch (NoSuchMethodException e) {
      // Entity doesn't have sessionId, this is normal for some entities
      throw new SessionOperationException("Error executing getSessionId method", e);
    } catch (Exception e) {
      log.error("Error verifying sessionId: {}", e.getMessage());
      throw new SessionOperationException("Error executing getSessionId method", e);
    }
  }

  /** Sets the session ID on an entity using reflection. */
  @Transactional
  private void setEntitySessionId(
      Object entity, String sessionId, JpaRepository<Object, Long> repository, boolean saveEntity) {
    try {
      Method setSessionId = entity.getClass().getMethod("setSessionId", String.class);
      setSessionId.invoke(entity, sessionId);

      if (saveEntity && repository != null) {
        repository.save(entity);
      }
    } catch (NoSuchMethodException e) {
      // Entity doesn't have setSessionId method, this is normal for some entities
      log.debug("Entity {} doesn't have setSessionId method", entity.getClass().getSimpleName());
    } catch (IllegalAccessException e) {
      log.error(
          "Illegal access to setSessionId method on {}: {}",
          entity.getClass().getSimpleName(),
          e.getMessage());
      throw new SessionOperationException("Illegal access to setSessionId", e);
    } catch (java.lang.reflect.InvocationTargetException e) {
      log.error(
          "Error invoking setSessionId on {}: {}",
          entity.getClass().getSimpleName(),
          e.getCause().getMessage());
      throw new SessionOperationException("Error invoking setSessionId", e.getCause());
    } catch (Exception e) {
      log.error(
          "Unexpected error setting sessionId on {}: {}",
          entity.getClass().getSimpleName(),
          e.getMessage());
      throw new SessionOperationException("Unexpected error setting sessionId", e);
    }
  }

  /** Recreates an entity from its state map. */
  @Transactional
  private Object recreateEntityFromState(String entityType, Map<String, Object> state) {
    if (state == null) return null;

    try {
      Class<?> entityClass = findEntityClassForType(entityType);
      if (entityClass == null) {
        log.error("Unable to find entity class for type: {}", entityType);
        return null;
      }

      Object entity = entityClass.getDeclaredConstructor().newInstance();
      restoreEntityFromState(entity, state);
      return entity;
    } catch (Exception e) {
      log.error("Error recreating entity {}: {}", entityType, e.getMessage());
      return null;
    }
  }

  /** Finds the entity class for a given entity type. */
  private Class<?> findEntityClassForType(String entityType) {
    for (Map.Entry<String, JpaRepository<?, Long>> entry : repositories.entrySet()) {
      if (entry.getKey().equals(entityType)) {
        return getEntityClass(entry.getValue());
      }
    }
    return null;
  }

  /** Creates a session expiry time based on the current time. */
  private LocalDateTime createExpiryTime() {
    return LocalDateTime.now().plusHours(SESSION_EXPIRY_HOURS);
  }

  /** Retrieves an entity by its ID from the appropriate repository. */
  @SuppressWarnings("unchecked")
  private Object getEntityById(String entityType, Long entityId) {
    JpaRepository<Object, Long> repository =
        (JpaRepository<Object, Long>) repositories.get(entityType);
    if (repository == null) {
      throw new IllegalArgumentException("Unsupported entity type: " + entityType);
    }

    return repository
        .findById(entityId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Entity not found: " + entityType + " with ID " + entityId));
  }

  /** Creates an audit entry for a session operation. */
  private EditSessionAudit createAuditEntry(
      String sessionId,
      String entityType,
      Long entityId,
      String action,
      Map<String, Object> previousState,
      LocalDateTime expiresAt) {
    return EditSessionAudit.builder()
        .sessionId(sessionId)
        .entityType(entityType)
        .entityId(entityId)
        .action(action)
        .previousState(previousState)
        .expiresAt(expiresAt)
        .build();
  }

  /** Gets the expiry time of a session. */
  @Transactional(readOnly = true)
  public LocalDateTime getSessionExpiryTime(String sessionId) {
    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
    if (audits.isEmpty()) {
      throw new SessionNotFoundException("Session not found: " + sessionId);
    }

    return audits.stream()
        .map(EditSessionAudit::getExpiresAt)
        .max(LocalDateTime::compareTo)
        .orElse(LocalDateTime.now().plusHours(SESSION_EXPIRY_HOURS));
  }

  /** Starts a creation session for a new entity. */
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public String startCreateSession(String entityType) {
    String sessionId = UUID.randomUUID().toString();
    LocalDateTime expiresAt = createExpiryTime();

    // Creation session uses a special entity ID of 0
    EditSessionAudit audit =
        createAuditEntry(sessionId, entityType, 0L, CREATE_SESSION_ACTION, null, expiresAt);
    auditRepository.save(audit);

    return sessionId;
  }

  /** Starts a session for updating an existing entity. */
  @Transactional
  public String startSession(String entityType, Long entityId) {
    // Generate unique session ID
    String sessionId = UUID.randomUUID().toString();
    LocalDateTime expiresAt = createExpiryTime();

    // Get entity and repository
    JpaRepository<Object, Long> repository =
        (JpaRepository<Object, Long>) repositories.get(entityType);
    if (repository == null) {
      throw new IllegalArgumentException("Unsupported entity type: " + entityType);
    }

    Object entity = getEntityById(entityType, entityId);

    // Verify entity isn't already in a session
    extractSessionIdFromEntity(entity);

    // Create state snapshot for rollback
    Map<String, Object> entityState = objectMapper.convertValue(entity, Map.class);
    removeComplexRelations(entityType, entityState);

    // Create audit entry
    EditSessionAudit audit =
        createAuditEntry(sessionId, entityType, entityId, UPDATE_ACTION, entityState, expiresAt);
    auditRepository.save(audit);

    // Mark entity as being in session
    setEntitySessionId(entity, sessionId, repository, true);

    return sessionId;
  }

  /** Confirms a session, applying all changes permanently. */
  @Transactional(
      propagation = Propagation.REQUIRED,
      isolation = Isolation.READ_COMMITTED,
      rollbackFor = Exception.class)
  public void confirmSession(String sessionId) {
    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);

    // Clear session IDs from entities
    clearSessionIdsFromEntities(audits);

    // Remove audit entries
    auditRepository.deleteBySessionId(sessionId);
  }

  /** Clears session IDs from all entities in a session. */
  @SuppressWarnings("unchecked")
  private void clearSessionIdsFromEntities(List<EditSessionAudit> audits) {
    for (EditSessionAudit audit : audits) {
      if (DELETE_ACTION.equals(audit.getAction())) {
        continue; // Skip delete actions when confirming
      }

      JpaRepository<Object, Long> repository =
          (JpaRepository<Object, Long>) repositories.get(audit.getEntityType());

      if (repository != null) {
        repository
            .findById(audit.getEntityId())
            .ifPresent(entity -> setEntitySessionId(entity, null, repository, true));
      }
    }
  }

  /** Cancels a session, rolling back all changes. */
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void cancelSession(String sessionId) {
    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);

    // Group audits by entity type
    Map<String, List<EditSessionAudit>> auditsByType =
        audits.stream().collect(Collectors.groupingBy(EditSessionAudit::getEntityType));

    // Process in order according to entity dependencies
    List<String> processingOrder = determineProcessingOrder(auditsByType.keySet());
    for (String entityType : processingOrder) {
      processCancellationByEntityType(entityType, auditsByType.get(entityType));
    }

    // Remove audit entries
    auditRepository.deleteBySessionId(sessionId);
  }

  /** Processes cancellation for a specific entity type. */
  @SuppressWarnings("unchecked")
  private void processCancellationByEntityType(String entityType, List<EditSessionAudit> audits) {
    JpaRepository<Object, Long> repository =
        (JpaRepository<Object, Long>) repositories.get(entityType);
    if (repository == null) return;

    for (EditSessionAudit audit : audits) {
      switch (audit.getAction()) {
        case CREATE_ACTION:
          // Delete created entities
          repository.findById(audit.getEntityId()).ifPresent(repository::delete);
          break;
        case UPDATE_ACTION:
          // Restore previous state
          repository
              .findById(audit.getEntityId())
              .ifPresent(
                  entity -> {
                    restoreEntityFromState(entity, audit.getPreviousState());
                    repository.save(entity);
                  });
          break;
        case DELETE_ACTION:
          // Recreate deleted entities
          Object recreatedEntity = recreateEntityFromState(entityType, audit.getPreviousState());
          if (recreatedEntity != null) {
            repository.save(recreatedEntity);
          }
          break;
      }
    }
  }

  /** Cleans up expired sessions periodically. */
  @Transactional
  @Scheduled(fixedRate = 3600000) // Execute every hour
  public void cleanupExpiredSessions() {
    LocalDateTime now = LocalDateTime.now();
    List<EditSessionAudit> expiredAudits = auditRepository.findByExpiresAtBefore(now);

    // Group by session ID
    Map<String, List<EditSessionAudit>> auditsBySession =
        expiredAudits.stream().collect(Collectors.groupingBy(EditSessionAudit::getSessionId));

    // Process each expired session
    for (String sessionId : auditsBySession.keySet()) {
      try {
        cleanupProductImages(sessionId, auditsBySession.get(sessionId));
        cancelSession(sessionId);
        log.info("Expired session cancelled: {}", sessionId);
      } catch (Exception e) {
        log.error("Error cancelling expired session {}: {}", sessionId, e.getMessage());
      }
    }
  }

  /** Cleans up product images associated with expired sessions. */
  private void cleanupProductImages(String sessionId, List<EditSessionAudit> sessionAudits) {
    List<EditSessionAudit> productAudits =
        sessionAudits.stream()
            .filter(audit -> "PRODUCT".equals(audit.getEntityType()))
            .collect(Collectors.toList());

    for (EditSessionAudit productAudit : productAudits) {
      String deleteImagesQuery = "DELETE FROM product_images WHERE product_id = ?";
      entityManager
          .createNativeQuery(deleteImagesQuery)
          .setParameter(1, productAudit.getEntityId())
          .executeUpdate();
    }
  }

  /** Checks if a session is valid (exists and not expired). */
  @Transactional(readOnly = true)
  public boolean isSessionValid(String sessionId) {
    if (sessionId == null || sessionId.isEmpty()) {
      return false;
    }

    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
    if (audits.isEmpty()) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();
    return audits.stream().anyMatch(audit -> audit.getExpiresAt().isAfter(now));
  }

  /** Validates that a session exists and is not expired. */
  private void validateSession(String sessionId) {
    if (!isSessionValid(sessionId)) {
      throw new SessionExpiredException("Session has expired or doesn't exist");
    }
  }

  /** Registers the creation of a new entity in a session. */
  @Transactional
  public void registerEntityCreation(String sessionId, String entityType, Long entityId) {
    validateSession(sessionId);

    LocalDateTime expiresAt = getSessionExpiryTime(sessionId);
    EditSessionAudit audit =
        createAuditEntry(sessionId, entityType, entityId, CREATE_ACTION, null, expiresAt);

    auditRepository.save(audit);
  }

  /** Registers the modification of an entity in a session. */
  @Transactional
  public void registerEntityModification(
      String sessionId, String entityType, Long entityId, Object entity) {
    validateSession(sessionId);

    LocalDateTime expiresAt = getSessionExpiryTime(sessionId);

    // Create state snapshot
    Map<String, Object> entityState = objectMapper.convertValue(entity, Map.class);
    removeComplexRelations(entityType, entityState);

    // Create audit entry
    EditSessionAudit audit =
        createAuditEntry(sessionId, entityType, entityId, UPDATE_ACTION, entityState, expiresAt);

    auditRepository.save(audit);
  }

  /** Gets the expiry time of a session. */
  @Override
  public LocalDateTime getExpiryTime(String sessionId) {
    if (sessionId == null || sessionId.trim().isEmpty()) {
      throw new IllegalArgumentException("Session identifier cannot be null or empty");
    }

    List<EditSessionAudit> sessions = auditRepository.findBySessionId(sessionId);
    if (sessions.isEmpty()) {
      throw new SessionNotFoundException("Session not found: " + sessionId);
    }

    return sessions.get(0).getExpiresAt();
  }

  /** Finds a product by its ID. */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public Product findProductById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("L'ID du produit ne peut pas être nul");
    }

    JpaRepository<?, Long> repository = repositories.get("PRODUCT");
    if (repository == null) {
      throw new IllegalStateException("Aucun repository n'est enregistré pour le type PRODUCT");
    }

    return (Product)
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID: " + id));
  }
}
