package com.ecommerce.service.impl;

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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.exception.SessionNotFoundException;
import com.ecommerce.model.entity.EditSessionAudit;
import com.ecommerce.model.entity.SessionAware;
import com.ecommerce.repository.EditSessionAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EditSessionService {
    
    private final Map<String, JpaRepository<?, Long>> repositories;
    private final EditSessionAuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public EditSessionService(
        List<JpaRepository<?, Long>> repositoryList,
        EditSessionAuditRepository auditRepository,
        ObjectMapper objectMapper,
        EntityManager entityManager
    ) {
        this.repositories = new HashMap<>();

        for (JpaRepository<?, Long> repository: repositoryList) {
            Class<?>  entityClass = getEntityClass(repository);
            if(entityClass != null) {
                String entityType = entityClass.getSimpleName().toUpperCase();
                this.repositories.put(entityType, repository);
                log.info("Repository enregistré pour l'entité: {}", entityType);
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
            log.warn("Impossible de déterminer la classe d'entité pour le repository: {}", repository.getClass().getName());
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

    @Transactional
    public String startCreateSession(String entityType) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        
        // Créer une entrée d'audit spéciale pour la session de création
        EditSessionAudit audit = EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(0L)  // ID spécial pour indiquer une création
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

        JpaRepository<Object, Long> repository = (JpaRepository<Object, Long>) repositories.get(entityType);
        if (repository == null) {
            throw new IllegalArgumentException("Type d'entité non supporté: " + entityType);
        }
        
        Object entity = repository.findById(entityId)
            .orElseThrow(() -> new EntityNotFoundException("Entité non trouvée: " + entityType + " avec ID " + entityId));
    
        // if (entity instanceof SessionAware) {
        //     Map<String, Object> sessionInfo = ((SessionAware) entity).getSessionInfo();
        //     if (sessionInfo != null && sessionInfo.containsKey("sessionId")) {
        //         throw new EntityLockedException("L'entité est déjà en cours d'édition dans une autre session");
        //     }
        // }
        
        Map<String, Object> entityState = objectMapper.convertValue(entity, Map.class);
        removeComplexRelations(entityType, entityState);
        
        EditSessionAudit audit = EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(entityId)
            .action("UPDATE")
            .previousState(entityState)
            .expiresAt(expiresAt)
            .build();
        
        auditRepository.save(audit);
        
        if (entity instanceof SessionAware) {
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("sessionId", sessionId);
            sessionInfo.put("expiresAt", expiresAt);
            ((SessionAware) entity).setSessionInfo(sessionInfo);
            repository.save(entity);
        }

        return sessionId;
    }

    @Transactional
    public void confirmSession(String sessionId) {
        List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
        
        for (EditSessionAudit audit : audits) {
            JpaRepository<Object, Long> repository = (JpaRepository<Object, Long>) repositories.get(audit.getEntityType());
            
            if (repository != null && !"DELETE".equals(audit.getAction())) {
                repository.findById(audit.getEntityId()).ifPresent(entity -> {
                    if (entity instanceof SessionAware) {
                        ((SessionAware) entity).clearSessionInfo();
                        repository.save(entity);
                    }
                });
            }
        }
        
        auditRepository.deleteBySessionId(sessionId);
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
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        } else if (targetType == Long.class || targetType == long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
        } else if (targetType == LocalDateTime.class && value instanceof String) {
            return LocalDateTime.parse((String) value);
        } else if (targetType.isEnum() && value instanceof String) {
            return Enum.valueOf((Class<Enum>) targetType, (String) value);
        }
        
        // Pour les types complexes, utiliser Jackson
        return objectMapper.convertValue(value, targetType);
    }
    
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
                log.debug("Impossible de restaurer la propriété {} sur l'entité {}: {}", 
                    property, entity.getClass().getSimpleName(), e.getMessage());
            }
        }
        
        if (entity instanceof SessionAware) {
            ((SessionAware) entity).clearSessionInfo();
        }
    }

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
                log.error("Impossible de trouver la classe d'entité pour le type: {}", entityType);
                return null;
            }
            
            Object entity = entityClass.getDeclaredConstructor().newInstance();
            
            restoreEntityFromState(entity, state);
            
            return entity;
        } catch (Exception e) {
            log.error("Erreur lors de la recréation de l'entité {}: {}", entityType, e.getMessage());
            return null;
        }
    }


    @Transactional
    public void cancelSession(String sessionId) {
        List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
        
        Map<String, List<EditSessionAudit>> auditsByType = audits.stream()
            .collect(Collectors.groupingBy(EditSessionAudit::getEntityType));
        
        List<String> processingOrder = determineProcessingOrder(auditsByType.keySet());
        
        for (String entityType : processingOrder) {
            List<EditSessionAudit> typeAudits = auditsByType.get(entityType);
            JpaRepository<Object, Long> repository = (JpaRepository<Object, Long>) repositories.get(entityType);
            
            for (EditSessionAudit audit : typeAudits) {
                if ("CREATE".equals(audit.getAction())) {
                    
                    repository.findById(audit.getEntityId())
                        .ifPresent(repository::delete);
                } else if ("UPDATE".equals(audit.getAction())) {
                    repository.findById(audit.getEntityId())
                        .ifPresent(entity -> {
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

    @Scheduled(fixedRate = 3600000) // Exécution toutes les heures
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<EditSessionAudit> expiredAudits = auditRepository.findByExpiresAtBefore(now);
        
        Map<String, List<EditSessionAudit>> auditsBySession = expiredAudits.stream()
            .collect(Collectors.groupingBy(EditSessionAudit::getSessionId));
        
        for (String sessionId : auditsBySession.keySet()) {
            try { 
                cancelSession(sessionId);
                log.info("Session expirée annulée: {}", sessionId);
            } catch (Exception e) {
                log.error("Erreur lors de l'annulation de la session expirée {}: {}", sessionId, e.getMessage());
            }
        }
    }

    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        
        List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
        if (audits.isEmpty()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return audits.stream()
            .anyMatch(audit -> audit.getExpiresAt().isAfter(now));
    }

    @Transactional
    public void registerEntityCreation(String sessionId, String entityType, Long entityId) {
        if (!isSessionValid(sessionId)) {
            throw new SessionExpiredException("La session a expiré ou n'existe pas");
        }
        
        LocalDateTime expiresAt = getSessionExpiryTime(sessionId);
        
        EditSessionAudit audit = EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(entityId)
            .action("CREATE")
            .expiresAt(expiresAt)
            .build();
        
        auditRepository.save(audit);
    }

    @Transactional void registerEntityModification(String sessionId, String entityType, Long entityId, Object entity) {
        if (!isSessionValid(sessionId)) {
            throw new SessionExpiredException("La session a expiré ou n'existe pas");
        }

        LocalDateTime expiresAt = getSessionExpiryTime(sessionId);
        Map<String, Object> entityState = objectMapper.convertValue(entity, Map.class);
        removeComplexRelations(entityType, entityState);

        EditSessionAudit audit = EditSessionAudit.builder()
            .sessionId(sessionId)
            .entityType(entityType)
            .entityId(entityId)
            .action("UPDATE")
            .previousState(entityState)
            .expiresAt(expiresAt)
            .build();
        
        auditRepository.save(audit);
    }


    private LocalDateTime getSessionExpiryTime(String sessionId) {
        List<EditSessionAudit> audits = auditRepository.findBySessionId(sessionId);
        if (audits.isEmpty()) {
            throw new SessionNotFoundException("Session non trouvée: " + sessionId);
        }
        
        return audits.stream()
            .map(EditSessionAudit::getExpiresAt)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now().plusHours(24));
    }
}
// [
//     ProductVariant(id=164, product=null, sku=132-c-laine, price=45354.00, stockQuantity=504, sessions=null, attributeValues=[]), 
//     ProductVariant(id=165, product=null, sku=132-c-polyester, price=45354.00, stockQuantity=504, sessions=null, attributeValues=[]), 
//     ProductVariant(id=166, product=null, sku=132-d-laine, price=45354.00, stockQuantity=504, sessions=null, attributeValues=[]), 
//     ProductVariant(id=167, product=null, sku=132-d-polyester, price=45354.00, stockQuantity=504, sessions=null, attributeValues=[])
// ]