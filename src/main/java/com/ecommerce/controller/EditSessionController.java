package com.ecommerce.controller;

import com.ecommerce.exception.EntityLockedException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.exception.SessionNotFoundException;
import com.ecommerce.model.dto.SessionResponseDTO;
import com.ecommerce.model.dto.SessionStartDTO;
import com.ecommerce.service.impl.EditSessionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class EditSessionController {

  private final EditSessionServiceImpl editSessionServiceImpl;

  /** Starts an edit session */
  @PostMapping("/start")
  public ResponseEntity<?> startSession(@RequestBody SessionStartDTO request) {
    try {
      String sessionId =
          editSessionServiceImpl.startSession(request.getEntityType(), request.getEntityId());

      SessionResponseDTO response =
          new SessionResponseDTO(
              sessionId,
              request.getEntityType(),
              request.getEntityId(),
              null // expiresAt will be filled by the service
              );

      return ResponseEntity.ok(response);
    } catch (EntityLockedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /** Confirms session modifications */
  @PostMapping("/{sessionId}/confirm")
  public ResponseEntity<?> confirmSession(@PathVariable String sessionId) {
    try {
      editSessionServiceImpl.confirmSession(sessionId);
      return ResponseEntity.ok().build();
    } catch (SessionNotFoundException | SessionExpiredException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  /** Cancels session modifications */
  @PostMapping("/{sessionId}/cancel")
  public ResponseEntity<?> cancelSession(@PathVariable String sessionId) {
    try {
      editSessionServiceImpl.cancelSession(sessionId);
      return ResponseEntity.ok().build();
    } catch (SessionNotFoundException | SessionExpiredException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  /** Checks if a session is valid */
  @GetMapping("/{sessionId}/validate")
  public ResponseEntity<?> validateSession(@PathVariable String sessionId) {
    boolean isValid = editSessionServiceImpl.isSessionValid(sessionId);
    return ResponseEntity.ok(isValid);
  }

  /** Starts a session to create a new entity */
  @PostMapping("/start-create")
  public ResponseEntity<?> startCreateSession(@RequestBody SessionStartDTO request) {
    try {
      String sessionId = editSessionServiceImpl.startCreateSession(request.getEntityType());

      SessionResponseDTO response =
          new SessionResponseDTO(
              sessionId,
              request.getEntityType(),
              null, // No entity ID as this is a creation
              null // expiresAt will be filled by the service
              );

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
