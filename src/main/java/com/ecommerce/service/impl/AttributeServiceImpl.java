package com.ecommerce.service.impl;

import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.service.interfaces.AttributeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttributeServiceImpl implements AttributeService {

  private final AttributeRepository attributeRepository;

  @Override
  public List<Attribute> findAllAttributes() {
    return attributeRepository.findAll();
  }

  @Override
  public List<AttributeDTO> findAllWithValues() {
    return attributeRepository.findAllWithValues().stream()
        .<AttributeDTO>map(
            result -> {
              List<Map<String, Object>> valuesList = new ArrayList<>();
              if (result[2] != null) {
                for (String value : ((String) result[2]).split(",")) {
                  Map<String, Object> valueMap = new HashMap<>();
                  valueMap.put("value", value);
                  valuesList.add(valueMap);
                }
              }

              return AttributeDTO.builder()
                  .id(((Number) result[0]).longValue())
                  .name((String) result[1])
                  .values(valuesList)
                  .build();
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<AttributeDTO> findAttributesWithValuesByIds(List<Long> attributeIds) {
    List<Object[]> results = attributeRepository.findAttributesWithValuesByIds(attributeIds);

    Map<Long, AttributeDTO> attributesMap = new HashMap<>();

    for (Object[] result : results) {
      Long attributeId = ((Number) result[0]).longValue();
      String attributeName = (String) result[1];
      String attributeValue = (String) result[2];
      Long valueId = result.length > 3 ? ((Number) result[3]).longValue() : null;

      AttributeDTO dto =
          attributesMap.computeIfAbsent(
              attributeId,
              id ->
                  AttributeDTO.builder()
                      .id(id)
                      .name(attributeName)
                      .values(new ArrayList<>())
                      .build());

      if (attributeValue != null && valueId != null) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", valueId);
        valueMap.put("value", attributeValue);
        dto.getValues().add(valueMap);
      }
    }

    return new ArrayList<>(attributesMap.values());
  }

  @Override
  @Transactional
  public AttributeDTO createAttributeWithValues(CreateAttributeDTO dto) {

    Attribute attribute = Attribute.builder().name(dto.getName()).build();

    final Attribute finalAttribute = attribute;
    Set<AttributeValue> attributeValues =
        dto.getValues().stream()
            .map(value -> AttributeValue.builder().attribute(finalAttribute).value(value).build())
            .collect(Collectors.toSet());

    attribute.setValues(attributeValues);

    attribute = attributeRepository.save(attribute);

    return AttributeDTO.builder()
        .id(attribute.getId())
        .name(attribute.getName())
        .values(
            attributeValues.stream()
                .map(
                    value -> {
                      Map<String, Object> valueMap = new HashMap<>();
                      valueMap.put("value", value.getValue());
                      return valueMap;
                    })
                .collect(Collectors.toList()))
        .build();
  }
}
