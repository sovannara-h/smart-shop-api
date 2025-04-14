package com.ecommerce.controller;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.service.interfaces.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for product attribute management. Provides endpoints to create and retrieve attributes
 * and their values.
 */
@RestController
@RequestMapping("/api/v1/attributes")
@Tag(name = "Attributes", description = "Attribute management APIs")
@Slf4j
@RequiredArgsConstructor
public class AttributeController {

  private final AttributeService attributeService;

  /**
   * Retrieves all attributes with their associated values.
   *
   * @return A response containing the list of all attributes with their values
   */
  @Operation(
      summary = "Get all attributes with values",
      description = "Returns all product attributes with their associated values")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Attributes retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error")
      })
  @GetMapping("/with-values")
  public ResponseEntity<ApiResponse<List<AttributeDTO>>> getAllWithValues() {
    try {
      List<AttributeDTO> attributes = attributeService.findAllWithValues();
      return ResponseEntity.ok()
          .body(
              new ApiResponse<>(
                  true,
                  attributes,
                  "Attributes retrieved successfully",
                  null,
                  LocalDateTime.now()));
    } catch (Exception e) {
      log.error("Error retrieving attributes with values", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ApiResponse<>(
                  false,
                  null,
                  null,
                  "Error retrieving attributes: " + e.getMessage(),
                  LocalDateTime.now()));
    }
  }

  /**
   * Creates a new attribute with its associated values.
   *
   * @param dto The attribute data to create
   * @return A response containing the created attribute with its values
   */
  @Operation(
      summary = "Create a new attribute with values",
      description = "Creates a new product attribute with its values")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Attribute created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error")
      })
  @PostMapping
  public ResponseEntity<ApiResponse<AttributeDTO>> createAttribute(
      @Parameter(description = "The attribute data including its values", required = true)
          @RequestBody
          CreateAttributeDTO dto) {
    try {
      AttributeDTO created = attributeService.createAttributeWithValues(dto);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              new ApiResponse<>(
                  true, created, "Attribute created successfully", null, LocalDateTime.now()));
    } catch (Exception e) {
      log.error("Error creating attribute", e);
      return ResponseEntity.badRequest()
          .body(
              new ApiResponse<>(
                  false,
                  null,
                  null,
                  "Error creating attribute: " + e.getMessage(),
                  LocalDateTime.now()));
    }
  }
}
