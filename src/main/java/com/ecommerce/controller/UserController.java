package com.ecommerce.controller;

import com.ecommerce.mapper.UserMapper;
import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.dto.UserDto;
import com.ecommerce.model.entity.User;
import com.ecommerce.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserServiceImpl userService;
  private final UserMapper userMapper;

  public UserController(UserServiceImpl userService, UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerUser(
      @Valid @RequestBody SignUpRequest signUpRequest) {
    User user = userService.registerUser(signUpRequest);
    UserDto userDto = userMapper.toDto(user);
    return buildSuccessResponse(userDto, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a user by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    User user = userService.findUserById(id);
    return ResponseEntity.ok(user);
  }

  @PostMapping
  @Operation(summary = "Create a new user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(responseCode = "400", description = "Invalid user data")
      })
  public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail(user.getEmail());
    signUpRequest.setPassword(user.getPassword());

    User createdUser = userService.registerUser(signUpRequest);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdUser.getId())
            .toUri();
    return ResponseEntity.created(location).body(createdUser);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid user data")
      })
  public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
    user.setId(id);
    User updatedUser = userService.updateUser(id, user);
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get all users")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = User.class)))
            })
      })
  public ResponseEntity<List<User>> getAllUsers() {
    // Note: Cette méthode devrait être implémentée dans le service
    throw new UnsupportedOperationException("Méthode non encore implémentée");
  }

  @GetMapping("/search")
  @Operation(summary = "Search users by email")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = User.class)))
            })
      })
  public ResponseEntity<List<User>> searchUsersByEmail(@RequestParam String email) {
    // Note: Cette méthode devrait être implémentée dans le service
    throw new UnsupportedOperationException("Méthode non encore implémentée");
  }

  private ResponseEntity<Map<String, Object>> buildSuccessResponse(Object data, HttpStatus status) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("data", data);
    response.put("timestamp", LocalDateTime.now());
    return ResponseEntity.status(status).body(response);
  }
}
