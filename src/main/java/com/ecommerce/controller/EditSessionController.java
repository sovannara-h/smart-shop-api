package com.ecommerce.controller;

import com.ecommerce.exception.EntityLockedException;
import com.ecommerce.exception.SessionExpiredException;
import com.ecommerce.exception.SessionNotFoundException;
import com.ecommerce.model.dto.SessionResponseDTO;
import com.ecommerce.model.dto.SessionStartDTO;
import com.ecommerce.service.interfaces.EditSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for edit session management. Handles starting, validating, and confirming/canceling
 * edit sessions.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Edit Sessions", description = "API for managing entity edit sessions")
@Slf4j
@RequiredArgsConstructor
public class EditSessionController {

  private final EditSessionService editSessionService;

  /**
   * Starts a new edit session.
   *
   * @param dto Data needed to start a session (entity type, identifier)
   * @return A response containing the created session identifier
   */
  @Operation(
      summary = "Start a new edit session",
      description = "Creates a new edit session for a specific entity")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Session started successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(
            responseCode = "409",
            description = "Entity is already locked by another session"),
        @ApiResponse(responseCode = "500", description = "Server error")
      })
  @PostMapping("/start")
  public ResponseEntity<SessionResponseDTO> startSession(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Information required to start a session",
              required = true,
              content = @Content(schema = @Schema(implementation = SessionStartDTO.class)))
          @RequestBody
          SessionStartDTO dto) {
    try {
      String sessionId = null;

      if (dto.getEntityId() != null) {
        // Editing an existing entity
        sessionId = editSessionService.startSession(dto.getEntityType(), dto.getEntityId());
      } else {
        // Creating a new entity
        sessionId = editSessionService.startCreateSession(dto.getEntityType());
      }

      return ResponseEntity.ok(new SessionResponseDTO(sessionId, true, null));
    } catch (EntityLockedException e) {
      log.warn("Entity already locked: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new SessionResponseDTO(null, false, e.getMessage()));
    } catch (Exception e) {
      log.error("Error starting session", e);
      return ResponseEntity.badRequest().body(new SessionResponseDTO(null, false, e.getMessage()));
    }
  }

  /**
   * Verifies if an edit session is valid and active.
   *
   * @param sessionId The session identifier to check
   * @return A response indicating whether the session is valid
   */
  @Operation(
      summary = "Verify session validity",
      description = "Checks if an edit session is still valid and active")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
      })
  @GetMapping("/validate/{sessionId}")
  public ResponseEntity<SessionResponseDTO> validateSession(
      @Parameter(description = "Session identifier to validate", required = true) @PathVariable
          String sessionId) {
    try {
      boolean isValid = editSessionService.isSessionValid(sessionId);
      return ResponseEntity.ok(new SessionResponseDTO(sessionId, isValid, null));
    } catch (SessionNotFoundException e) {
      log.warn("Session not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    } catch (Exception e) {
      log.error("Error validating session", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    }
  }

  /**
   * Confirms and finalizes an edit session, applying all modifications.
   *
   * @param sessionId The session identifier to confirm
   * @return A response indicating the success of the confirmation
   */
  @Operation(
      summary = "Confirm an edit session",
      description = "Finalizes an edit session and applies all modifications")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Session confirmed successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "410", description = "Session expired"),
        @ApiResponse(responseCode = "500", description = "Server error")
      })
  @PostMapping("/confirm/{sessionId}")
  public ResponseEntity<SessionResponseDTO> confirmSession(
      @Parameter(description = "Session identifier to confirm", required = true) @PathVariable
          String sessionId) {
    try {
      editSessionService.confirmSession(sessionId);
      return ResponseEntity.ok(new SessionResponseDTO(sessionId, true, "Session confirmed"));
    } catch (SessionNotFoundException e) {
      log.warn("Session not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    } catch (SessionExpiredException e) {
      log.warn("Session expired: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.GONE)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    } catch (Exception e) {
      log.error("Error confirming session", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    }
  }

  /**
   * Cancels an edit session, reverting all modifications.
   *
   * @param sessionId The session identifier to cancel
   * @return A response indicating the success of the cancellation
   */
  @Operation(
      summary = "Cancel an edit session",
      description = "Cancels an edit session and reverts to the previous state")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Session canceled successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "410", description = "Session expired"),
        @ApiResponse(responseCode = "500", description = "Server error")
      })
  @PostMapping("/cancel/{sessionId}")
  public ResponseEntity<SessionResponseDTO> cancelSession(
      @Parameter(description = "Session identifier to cancel", required = true) @PathVariable
          String sessionId) {
    try {
      editSessionService.cancelSession(sessionId);
      return ResponseEntity.ok(new SessionResponseDTO(sessionId, true, "Session canceled"));
    } catch (SessionNotFoundException e) {
      log.warn("Session not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    } catch (SessionExpiredException e) {
      log.warn("Session expired: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.GONE)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    } catch (Exception e) {
      log.error("Error canceling session", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SessionResponseDTO(sessionId, false, e.getMessage()));
    }
  }
}
