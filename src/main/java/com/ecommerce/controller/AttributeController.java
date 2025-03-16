package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.service.impl.AttributeServiceImpl;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/attributes")
@Tag(name="Attributes", description = "Gestion des attributs")
@Slf4j
@RequiredArgsConstructor
public class AttributeController {
    
    private final AttributeServiceImpl attributeService;
    
    @GetMapping("/with-values")
    public ResponseEntity<ApiResponse<List<AttributeDTO>>> getAllAttributes() {
        try {
            List<AttributeDTO> attributes = attributeService.findAllWithValues();
            return ResponseEntity.ok(new ApiResponse<>(
                true, attributes, "Attributs récupérés avec succès", null, LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des attributs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

    @PostMapping("/with-values")
    public ResponseEntity<ApiResponse<AttributeDTO>> createAttributeWithValues(@RequestBody CreateAttributeDTO createAttributeDTO) {
        try {
            AttributeDTO created = attributeService.createAttributeWithValues(createAttributeDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                true, created, "Attribut créé avec succès", null, LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'attribut: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }
}
