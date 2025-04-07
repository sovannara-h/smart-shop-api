package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.model.entity.Attribute;
import java.util.List;

/** Service for product attribute management */
public interface AttributeService {
  /**
   * Retrieves all product attributes
   *
   * @return list of all attributes
   */
  List<Attribute> findAllAttributes();

  /**
   * Retrieves all attributes with their values
   *
   * @return list of all attributes with their associated values
   */
  List<AttributeDTO> findAllWithValues();

  /**
   * Finds attributes with their values by attribute IDs
   *
   * @param attributeIds list of attribute identifiers
   * @return list of attributes with their associated values
   */
  List<AttributeDTO> findAttributesWithValuesByIds(List<Long> attributeIds);

  /**
   * Creates a new attribute with its values
   *
   * @param dto attribute creation data with values
   * @return the created attribute with its values
   */
  AttributeDTO createAttributeWithValues(CreateAttributeDTO dto);
}
