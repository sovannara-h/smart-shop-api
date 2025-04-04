package com.ecommerce.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class ProductImagesUpsertDTO {
  List<ProductImageUpsertDTO> productImages;
}
