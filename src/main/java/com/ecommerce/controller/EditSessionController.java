package com.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.exception.EntityLockedException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.exception.SessionNotFoundException;
import com.ecommerce.model.dto.SessionResponseDTO;
import com.ecommerce.model.dto.SessionStartDTO;
import com.ecommerce.service.impl.EditSessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class EditSessionController {

    private final EditSessionService editSessionService;

    /**
     * Démarre une session d'édition
     */
    @PostMapping("/start")
    public ResponseEntity<?> startSession(@RequestBody SessionStartDTO request) {
        try {
            String sessionId = editSessionService.startSession(
                request.getEntityType(),
                request.getEntityId()
            );
            
            SessionResponseDTO response = new SessionResponseDTO(
                sessionId,
                request.getEntityType(),
                request.getEntityId(),
                null  // expiresAt sera rempli par le service
            );
            
            return ResponseEntity.ok(response);
        } catch (EntityLockedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(e.getMessage());
        }
    }
    
    /**
     * Confirme les modifications d'une session
     */
    @PostMapping("/{sessionId}/confirm")
    public ResponseEntity<?> confirmSession(@PathVariable String sessionId) {
        try {
            editSessionService.confirmSession(sessionId);
            return ResponseEntity.ok().build();
        } catch (SessionNotFoundException | SessionExpiredException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    
    /**
     * Annule les modifications d'une session
     */
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<?> cancelSession(@PathVariable String sessionId) {
        try {
            editSessionService.cancelSession(sessionId);
            return ResponseEntity.ok().build();
        } catch (SessionNotFoundException | SessionExpiredException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    
    /**
     * Vérifie si une session est valide
     */
    @GetMapping("/{sessionId}/validate")
    public ResponseEntity<?> validateSession(@PathVariable String sessionId) {
        boolean isValid = editSessionService.isSessionValid(sessionId);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Démarre une session pour créer une nouvelle entité
     */
    @PostMapping("/start-create")
    public ResponseEntity<?> startCreateSession(@RequestBody SessionStartDTO request) {
        try {
            String sessionId = editSessionService.startCreateSession(request.getEntityType());
            
            SessionResponseDTO response = new SessionResponseDTO(
                sessionId,
                request.getEntityType(),
                null,  // Pas d'ID d'entité car c'est une création
                null   // expiresAt sera rempli par le service
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
