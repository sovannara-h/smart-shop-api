package com.ecommerce.model.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeDTO {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;
    
    @NotEmpty(message = "Au moins une valeur est requise")
    private List<String> values;
} 