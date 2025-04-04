package com.ecommerce.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
  @NotBlank(message = "L'email est obligatoire")
  @Email(message = "Format d'email invalide")
  @Size(max = 50)
  private String email;

  @NotBlank(message = "Le mot de passe est obligatoire")
  @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
  @Size(max = 100)
  private String password;
}
