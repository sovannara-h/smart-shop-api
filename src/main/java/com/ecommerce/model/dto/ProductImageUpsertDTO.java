package com.ecommerce.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class ProductImageUpsertDTO {
  private Long id;
  private String imageId;
  private String filename;
  private String sessionId;
  private List<String> variant;
}
