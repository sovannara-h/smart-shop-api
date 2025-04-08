package com.ecommerce.service.impl;

import com.ecommerce.exception.EntityLockedException;
import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.exception.SessionNotFoundException;
import com.ecommerce.exception.SessionOperationException;
import com.ecommerce.model.entity.EditSessionAudit;
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

@Service
@Slf4j
public class EditSessionServiceImpl implements EditSessionService {

  private final Map<String, JpaRepository<?, Long>> repositories;
  private final EditSessionAuditRepository auditRepository;
  private final ObjectMapper objectMapper;
  private final EntityManager entityManager;

  public EditSessionServiceImpl(
      List<JpaRepository<?, Long>> repositoryList,
      EditSessionAuditRepository auditRepository,
      ObjectMapper objectMapper,
      EntityManager entityManager) {
    this.repositories = new HashMap<>();

    for (JpaRepository<?, Long> repository : repositoryList) {
      Class<?> entityClass = getEntityClass(repository);
      if (entityClass != null) {
        String entityType = entityClass.getSimpleName().toUpperCase();
        this.repositories.put(entityType, repository);
        log.info("Repository registered for entity: {}", entityType);
      }
    }

    this.auditRepository = auditRepository;
    this.objectMapper = objectMapper;
    this.entityManager = entityManager;
  }

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

  private void removeComplexRelations(String entityType, Map<String, Object> entityState) {

    if ("PRODUCT".equals(entityType)) {
      entityState.remove("variants");
      entityState.remove("categories");
    } else if ("CATEGORY".equals(entityType)) {
      entityState.remove("products");
      entityState.remove("children");
      entityState.remove("parent");
    }

    entityState.remove("hibernateLazyInitializer");
  }

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

  private Object convertValueToType(Object value, Class<?> targetType) {
    if (value == null) return null;

    if (targetType.isAssignableFrom(value.getClass())) {
      return value;
    }

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

    // For complex types, use Jackson
    return objectMapper.convertValue(value, targetType);
  }

  @Transactional
  private void restoreEntityFromState(Object entity, Map<String, Object> state) {
    if (state == null) return;

    for (Map.Entry<String, Object> entry : state.entrySet()) {
      String property = entry.getKey();
      Object value = entry.getValue();

      if (property.equals("id") || property.equals("version")) continue;

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

    setEntitySessionId(entity, null, null, false);
  }

  @Transactional(readOnly = true)
  private String _getSessionId(Object entity) {
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

  /**
   * Sets sessionId on an entity via reflection and saves the entity if requested
   *
   * @param entity The entity to modify
   * @param sessionId The value of sessionId (can be null to clear)
   * @param repository The repository to save the entity
   * @param saveEntity Indicates if the entity should be saved
   * @throws EntitySessionException If an error occurs when invoking the method
   */
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

  @Transactional
  private Object recreateEntityFromState(String entityType, Map<String, Object> state) {
    if (state == null) return null;

    try {
      Class<?> entityClass = null;
      for (Map.Entry<String, JpaRepository<?, Long>> entry : repositories.entrySet()) {
        if (entry.getKey().equals(entityType)) {
          entityClass = getEntityClass(entry.getValue());
          break;
        }
      }

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

  @Transactional(readOnly = true)
  public LocalDateTime getSessionExpiryTime(String sessionId) {
    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
    if (audits.isEmpty()) {
      throw new SessionNotFoundException("Session not found: " + sessionId);
    }

    return audits.stream()
        .map(EditSessionAudit::getExpiresAt)
        .max(LocalDateTime::compareTo)
        .orElse(LocalDateTime.now().plusHours(24));
  }

  @Transactional
  public String startCreateSession(String entityType) {
    String sessionId = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

    // Create special audit entry for creation session
    EditSessionAudit audit =
        EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(0L) // Special
            // ID
            // for
            // indicating
            // creation
            .action("CREATE_SESSION")
            .expiresAt(expiresAt)
            .build();

    auditRepository.save(audit);

    return sessionId;
  }

  @Transactional
  public String startSession(String entityType, Long entityId) {
    String sessionId = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

    JpaRepository<Object, Long> repository =
        (JpaRepository<Object, Long>) repositories.get(entityType);
    if (repository == null) {
      throw new IllegalArgumentException("Unsupported entity type: " + entityType);
    }

    Object entity =
        repository
            .findById(entityId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Entity not found: " + entityType + " with ID " + entityId));

    _getSessionId(entity);

    Map<String, Object> entityState = objectMapper.convertValue(entity, Map.class);
    removeComplexRelations(entityType, entityState);

    EditSessionAudit audit =
        EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(entityId)
            .action("UPDATE")
            .previousState(entityState)
            .expiresAt(expiresAt)
            .build();

    auditRepository.save(audit);

    setEntitySessionId(entityState, sessionId, repository, true);

    return sessionId;
  }

  @Transactional(
      propagation = Propagation.REQUIRED,
      isolation = Isolation.READ_COMMITTED,
      rollbackFor = Exception.class)
  public void confirmSession(String sessionId) {
    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);

    for (EditSessionAudit audit : audits) {
      JpaRepository<Object, Long> repository =
          (JpaRepository<Object, Long>) repositories.get(audit.getEntityType());

      if (repository != null && !"DELETE".equals(audit.getAction())) {
        repository
            .findById(audit.getEntityId())
            .ifPresent(
                entity -> {
                  setEntitySessionId(entity, null, repository, true);
                });
      }
    }

    auditRepository.deleteBySessionId(sessionId);
  }

  @Transactional
  public void cancelSession(String sessionId) {
    List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);

    Map<String, List<EditSessionAudit>> auditsByType =
        audits.stream().collect(Collectors.groupingBy(EditSessionAudit::getEntityType));

    List<String> processingOrder = determineProcessingOrder(auditsByType.keySet());

    for (String entityType : processingOrder) {
      List<EditSessionAudit> typeAudits = auditsByType.get(entityType);
      JpaRepository<Object, Long> repository =
          (JpaRepository<Object, Long>) repositories.get(entityType);

      for (EditSessionAudit audit : typeAudits) {
        if ("CREATE".equals(audit.getAction())) {

          repository.findById(audit.getEntityId()).ifPresent(repository::delete);
        } else if ("UPDATE".equals(audit.getAction())) {
          repository
              .findById(audit.getEntityId())
              .ifPresent(
                  entity -> {
                    restoreEntityFromState(entity, audit.getPreviousState());
                    repository.save(entity);
                  });
        } else if ("DELETE".equals(audit.getAction())) {
          Object recreatedEntity = recreateEntityFromState(entityType, audit.getPreviousState());
          if (recreatedEntity != null) {
            repository.save(recreatedEntity);
          }
        }
      }
    }

    auditRepository.deleteBySessionId(sessionId);
  }

  @Transactional
  @Scheduled(fixedRate = 3600000) // Execution every hour
  public void cleanupExpiredSessions() {
    LocalDateTime now = LocalDateTime.now();
    List<EditSessionAudit> expiredAudits = auditRepository.findByExpiresAtBefore(now);

    Map<String, List<EditSessionAudit>> auditsBySession =
        expiredAudits.stream().collect(Collectors.groupingBy(EditSessionAudit::getSessionId));

    for (String sessionId : auditsBySession.keySet()) {
      try {
        // Supprimer d'abord les images des produits
        List<EditSessionAudit> productAudits =
            auditsBySession.get(sessionId).stream()
                .filter(audit -> "PRODUCT".equals(audit.getEntityType()))
                .collect(Collectors.toList());

        for (EditSessionAudit productAudit : productAudits) {
          // Supprimer toutes les images associées au produit
          String deleteImagesQuery = "DELETE FROM product_images WHERE product_id = ?";
          entityManager
              .createNativeQuery(deleteImagesQuery)
              .setParameter(1, productAudit.getEntityId())
              .executeUpdate();
        }

        cancelSession(sessionId);
        log.info("Expired session cancelled: {}", sessionId);
      } catch (Exception e) {
        log.error("Error cancelling expired session {}: {}", sessionId, e.getMessage());
      }
    }
  }

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

  @Transactional
  public void registerEntityCreation(String sessionId, String entityType, Long entityId) {
    if (!isSessionValid(sessionId)) {
      throw new SessionExpiredException("Session has expired or doesn't exist");
    }

    LocalDateTime expiresAt = getSessionExpiryTime(sessionId);

    EditSessionAudit audit =
        EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(entityId)
            .action("CREATE")
            .expiresAt(expiresAt)
            .build();

    auditRepository.save(audit);
  }

  @Transactional
  public void registerEntityModification(
      String sessionId, String entityType, Long entityId, Object entity) {
    if (!isSessionValid(sessionId)) {
      throw new SessionExpiredException("Session has expired or doesn't exist");
    }

    LocalDateTime expiresAt = getSessionExpiryTime(sessionId);
    Map<String, Object> entityState = objectMapper.convertValue(entity, Map.class);
    removeComplexRelations(entityType, entityState);

    EditSessionAudit audit =
        EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(entityId)
            .action("UPDATE")
            .previousState(entityState)
            .expiresAt(expiresAt)
            .build();

    auditRepository.save(audit);
  }
}
