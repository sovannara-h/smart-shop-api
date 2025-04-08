package com.ecommerce.controller;

import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;
import com.ecommerce.service.interfaces.AuthService;
import com.ecommerce.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
  private final UserService userServiceImpl;
  private final AuthService authServiceImpl;

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
    User user = userServiceImpl.registerUser(signUpRequest);
    return ResponseEntity.ok("Utilisateur enregistré avec succès : " + user.getEmail());
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(
      @RequestParam String email, @RequestParam String password) {
    String token = authServiceImpl.authenticate(email, password);
    return ResponseEntity.ok(token);
  }
}
