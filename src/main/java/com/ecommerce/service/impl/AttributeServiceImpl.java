package com.ecommerce.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.service.interfaces.AttributeService;

import lombok.RequiredArgsConstructor;

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
            .map(result -> AttributeDTO.builder()
                .id(((Number) result[0]).longValue())
                .name((String) result[1])
                .values(result[2] != null ? List.of(((String) result[2]).split(",")) : List.of())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public List<AttributeDTO> findAttributesWithValuesByIds(List<Long> attributeIds) {
        return attributeRepository.findAttributesWithValuesByIds(attributeIds).stream()
            .map(result -> AttributeDTO.builder()
                .id(((Number) result[0]).longValue())
                .name((String) result[1])
                .values(result[2] != null ? List.of(((String) result[2]).split(",")) : List.of())
                .build())
            .collect(Collectors.toList());
    }

    
    @Override
    @Transactional
    public AttributeDTO createAttributeWithValues(CreateAttributeDTO dto) {

        Attribute attribute = Attribute.builder()
            .name(dto.getName())
            .build();
        
        final Attribute finalAttribute = attribute;
        Set<AttributeValue> attributeValues = dto.getValues().stream()
            .map(value -> AttributeValue.builder()
                .attribute(finalAttribute)
                .value(value)
                .build())
            .collect(Collectors.toSet());
        

        attribute.setValues(attributeValues);
        
        attribute = attributeRepository.save(attribute);

        return AttributeDTO.builder()
            .id(attribute.getId())
            .name(attribute.getName())
            .values(attributeValues.stream()
                .map(AttributeValue::getValue)
                .collect(Collectors.toList()))
            .build();
    }


    
}
