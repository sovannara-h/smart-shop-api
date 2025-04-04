package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.model.entity.Attribute;
import java.util.List;

public interface AttributeService {
  List<Attribute> findAllAttributes();

  List<AttributeDTO> findAllWithValues();

  List<AttributeDTO> findAttributesWithValuesByIds(List<Long> attributeIds);

  AttributeDTO createAttributeWithValues(CreateAttributeDTO dto);
}
