package com.ecommerce.exception;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.ValidationErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the entire application. Provides centralized and consistent error
 * handling.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductNotFoundException(
      ProductNotFoundException ex, WebRequest request) {
    log.warn("Product not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(ProductException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductException(
      ProductException ex, WebRequest request) {
    log.error("Product-related error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {
    log.warn("User not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(OrderException.class)
  public ResponseEntity<ApiResponse<Object>> handleOrderException(
      OrderException ex, WebRequest request) {
    log.error("Order-related error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(SessionOperationException.class)
  public ResponseEntity<ApiResponse<Object>> handleSessionOperationException(
      SessionOperationException ex, WebRequest request) {
    log.error("Session operation error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(EntityLockedException.class)
  public ResponseEntity<ApiResponse<Object>> handleEntityLockedException(
      EntityLockedException ex, WebRequest request) {
    log.warn("Entity locked: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(ProductVariantException.class)
  public ResponseEntity<ApiResponse<Object>> handleProductVariantException(
      ProductVariantException ex, WebRequest request) {
    log.error("Product variant error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(ServiceOperationException.class)
  public ResponseEntity<ApiResponse<Object>> handleServiceOperationException(
      ServiceOperationException ex, WebRequest request) {
    log.error("Service operation error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<ApiResponse<Object>> handleOptimisticLockingFailureException(
      OptimisticLockingFailureException ex, WebRequest request) {
    log.warn("Concurrent modification detected: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ApiResponse<>(
                false,
                null,
                null,
                "La ressource a été modifiée par un autre utilisateur",
                LocalDateTime.now()));
  }

  @ExceptionHandler({EntityNotFoundException.class, CategoryNotFoundException.class})
  public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
      Exception ex, WebRequest request) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  /**
   * Handles validation errors from @Valid on RequestBody objects. Extracts and formats validation
   * errors for each field.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleValidationException(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, List<String>> errorsMap =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    ValidationErrorResponse validationErrors =
        new ValidationErrorResponse("Data validation error", errorsMap);

    log.warn("Validation errors: {}", errorsMap);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ApiResponse<>(
                false,
                validationErrors,
                null,
                "Validation errors were detected",
                LocalDateTime.now()));
  }

  /** Handles binding errors (for example, in forms) */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleBindException(
      BindException ex, WebRequest request) {

    Map<String, List<String>> errorsMap =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    ValidationErrorResponse validationErrors =
        new ValidationErrorResponse("Data validation error", errorsMap);

    log.warn("Binding errors: {}", errorsMap);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ApiResponse<>(
                false,
                validationErrors,
                null,
                "Validation errors were detected",
                LocalDateTime.now()));
  }

  /**
   * Handles constraint violations for validations outside RequestBody objects (such as method
   * parameters with @PathVariable, @RequestParam, etc.)
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {

    Map<String, List<String>> errorsMap =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.groupingBy(
                    violation -> extractPropertyPath(violation),
                    Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())));

    ValidationErrorResponse validationErrors =
        new ValidationErrorResponse("Data validation error", errorsMap);

    log.warn("Constraint violations: {}", errorsMap);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ApiResponse<>(
                false,
                validationErrors,
                null,
                "Validation errors were detected",
                LocalDateTime.now()));
  }

  /** Extracts property path for error display */
  private String extractPropertyPath(ConstraintViolation<?> violation) {
    String propertyPath = violation.getPropertyPath().toString();
    // Extract only the parameter/property name (last element of the path)
    int lastDotIndex = propertyPath.lastIndexOf('.');
    return lastDotIndex > 0 ? propertyPath.substring(lastDotIndex + 1) : propertyPath;
  }

  /** Handles type errors for method parameters */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {

    String errorMessage =
        String.format(
            "Parameter '%s' has incorrect type. Value '%s' cannot be converted to %s",
            ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

    log.warn("Parameter type error: {}", errorMessage);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, errorMessage, LocalDateTime.now()));
  }

  /** Handles missing request parameters */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, WebRequest request) {

    String errorMessage =
        String.format(
            "Parameter '%s' of type %s is required", ex.getParameterName(), ex.getParameterType());

    log.warn("Missing parameter: {}", errorMessage);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, errorMessage, LocalDateTime.now()));
  }

  /** Handles JSON or XML parsing errors in the request body */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, WebRequest request) {

    String errorMessage = "Incorrect data format. Please check your JSON/XML syntax";

    log.warn("Unreadable message: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, errorMessage, LocalDateTime.now()));
  }

  /** Handles unsupported HTTP methods */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {

    String errorMessage =
        String.format("HTTP method '%s' is not supported for this operation", ex.getMethod());

    log.warn("HTTP method not supported: {}", errorMessage);

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new ApiResponse<>(false, null, null, errorMessage, LocalDateTime.now()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    log.error("Invalid argument: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGenericException(
      Exception ex, WebRequest request) {
    log.error("Unhandled error: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse<>(false, null, null, "Internal server error", LocalDateTime.now()));
  }
}
