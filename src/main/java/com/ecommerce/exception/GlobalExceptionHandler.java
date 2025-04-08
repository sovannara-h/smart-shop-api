package com.ecommerce.exception;

import com.ecommerce.model.dto.ApiResponse;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Gestionnaire global d'exceptions pour toute l'application. Permet une gestion centralisée et
 * cohérente des erreurs.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductNotFoundException(
      ProductNotFoundException ex, WebRequest request) {
    log.warn("Produit non trouvé: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(ProductException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductException(
      ProductException ex, WebRequest request) {
    log.error("Erreur liée au produit: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {
    log.warn("Utilisateur non trouvé: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(OrderException.class)
  public ResponseEntity<ApiResponse<Object>> handleOrderException(
      OrderException ex, WebRequest request) {
    log.error("Erreur liée à la commande: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(SessionOperationException.class)
  public ResponseEntity<ApiResponse<Object>> handleSessionOperationException(
      SessionOperationException ex, WebRequest request) {
    log.error("Erreur d'opération de session: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(EntityLockedException.class)
  public ResponseEntity<ApiResponse<Object>> handleEntityLockedException(
      EntityLockedException ex, WebRequest request) {
    log.warn("Entité verrouillée: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(ProductVariantException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductVariantException(
      ProductVariantException ex, WebRequest request) {
    log.error("Erreur liée à la variante de produit: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(ServiceOperationException.class)
  public ResponseEntity<ApiResponse<Object>> handleServiceOperationException(
      ServiceOperationException ex, WebRequest request) {
    log.error("Erreur d'opération de service: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationException(
      MethodArgumentNotValidException ex, WebRequest request) {
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Erreur de validation");

    log.warn("Erreur de validation: {}", errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, errorMessage, LocalDateTime.now()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    log.error("Argument invalide: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGenericException(
      Exception ex, WebRequest request) {
    log.error("Erreur non gérée: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ApiResponse<>(false, null, null, "Erreur interne du serveur", LocalDateTime.now()));
  }
}
