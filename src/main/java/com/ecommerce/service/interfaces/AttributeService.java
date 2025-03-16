package com.ecommerce.service.interfaces;

import java.util.List;

import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.model.entity.Attribute;

public interface AttributeService {
    List<Attribute> findAllAttributes();
    List<AttributeDTO> findAllWithValues();
    List<AttributeDTO> findAttributesWithValuesByIds(List<Long> attributeIds);
    AttributeDTO createAttributeWithValues(CreateAttributeDTO dto);
}
