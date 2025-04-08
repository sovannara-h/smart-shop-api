package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.model.dto.AttributeDTO;
import com.ecommerce.model.dto.CreateAttributeDTO;
import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import com.ecommerce.repository.AttributeRepository;
import com.ecommerce.service.impl.AttributeServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AttributeServiceTest {

  @Mock private AttributeRepository attributeRepository;

  @InjectMocks private AttributeServiceImpl attributeService;

  private Attribute testAttribute;
  private List<Attribute> attributes;
  private Set<AttributeValue> attributeValues;
  private Object[] resultWithValues;
  private List<Object[]> resultsWithValues;

  @BeforeEach
  void setUp() {
    testAttribute = new Attribute();
    testAttribute.setId(1L);
    testAttribute.setName("Color");

    attributeValues = new HashSet<>();
    AttributeValue red = new AttributeValue();
    red.setId(1L);
    red.setValue("Red");
    red.setAttribute(testAttribute);

    AttributeValue blue = new AttributeValue();
    blue.setId(2L);
    blue.setValue("Blue");
    blue.setAttribute(testAttribute);

    attributeValues.add(red);
    attributeValues.add(blue);
    testAttribute.setValues(attributeValues);

    attributes = new ArrayList<>();
    attributes.add(testAttribute);

    // Créer les résultats pour les requêtes personnalisées
    resultWithValues = new Object[] {1L, "Color", "Red,Blue"};
    resultsWithValues = new ArrayList<>();
    resultsWithValues.add(new Object[] {1L, "Color", "Red", 1L});
    resultsWithValues.add(new Object[] {1L, "Color", "Blue", 2L});
  }

  @Test
  void findAllAttributes_Success() {
    // Given
    when(attributeRepository.findAll()).thenReturn(attributes);

    // When
    List<Attribute> result = attributeService.findAllAttributes();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Color", result.get(0).getName());
    assertEquals(2, result.get(0).getValues().size());
    verify(attributeRepository).findAll();
  }

  @Test
  void findAllWithValues_Success() {
    // Given
    List<Object[]> repositoryResults = new ArrayList<>();
    repositoryResults.add(resultWithValues);
    when(attributeRepository.findAllWithValues()).thenReturn(repositoryResults);

    // When
    List<AttributeDTO> result = attributeService.findAllWithValues();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals("Color", result.get(0).getName());
    assertEquals(2, result.get(0).getValues().size());

    List<String> values =
        result.get(0).getValues().stream()
            .map(map -> (String) map.get("value"))
            .collect(Collectors.toList());
    assertTrue(values.contains("Red"));
    assertTrue(values.contains("Blue"));

    verify(attributeRepository).findAllWithValues();
  }

  @Test
  void findAttributesWithValuesByIds_Success() {
    // Given
    List<Long> attributeIds = List.of(1L);
    when(attributeRepository.findAttributesWithValuesByIds(attributeIds))
        .thenReturn(resultsWithValues);

    // When
    List<AttributeDTO> result = attributeService.findAttributesWithValuesByIds(attributeIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals("Color", result.get(0).getName());
    assertEquals(2, result.get(0).getValues().size());

    // Vérifier que les valeurs d'attribut sont correctes
    List<String> values =
        result.get(0).getValues().stream()
            .map(map -> (String) map.get("value"))
            .collect(Collectors.toList());
    assertTrue(values.contains("Red"));
    assertTrue(values.contains("Blue"));

    // Vérifier que les IDs d'attribut sont correctes
    List<Long> valueIds =
        result.get(0).getValues().stream()
            .map(map -> (Long) map.get("id"))
            .collect(Collectors.toList());
    assertTrue(valueIds.contains(1L));
    assertTrue(valueIds.contains(2L));

    verify(attributeRepository).findAttributesWithValuesByIds(attributeIds);
  }

  @Test
  void findAttributesWithValuesByIds_EmptyList_ReturnsEmptyList() {
    // Given
    List<Long> attributeIds = Collections.emptyList();
    when(attributeRepository.findAttributesWithValuesByIds(attributeIds))
        .thenReturn(Collections.emptyList());

    // When
    List<AttributeDTO> result = attributeService.findAttributesWithValuesByIds(attributeIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(attributeRepository).findAttributesWithValuesByIds(attributeIds);
  }

  @Test
  void createAttributeWithValues_Success() {
    // Given
    CreateAttributeDTO dto = new CreateAttributeDTO();
    dto.setName("Size");
    dto.setValues(List.of("Small", "Medium", "Large"));

    Attribute createdAttribute = new Attribute();
    createdAttribute.setId(2L);
    createdAttribute.setName("Size");

    Set<AttributeValue> createdValues = new HashSet<>();
    AttributeValue small = new AttributeValue();
    small.setId(3L);
    small.setValue("Small");
    small.setAttribute(createdAttribute);

    AttributeValue medium = new AttributeValue();
    medium.setId(4L);
    medium.setValue("Medium");
    medium.setAttribute(createdAttribute);

    AttributeValue large = new AttributeValue();
    large.setId(5L);
    large.setValue("Large");
    large.setAttribute(createdAttribute);

    createdValues.add(small);
    createdValues.add(medium);
    createdValues.add(large);
    createdAttribute.setValues(createdValues);

    when(attributeRepository.save(any(Attribute.class))).thenReturn(createdAttribute);

    // When
    AttributeDTO result = attributeService.createAttributeWithValues(dto);

    // Then
    assertNotNull(result);
    assertEquals("Size", result.getName());
    assertEquals(3, result.getValues().size());

    List<String> values =
        result.getValues().stream()
            .map(map -> (String) map.get("value"))
            .collect(Collectors.toList());
    assertTrue(values.contains("Small"));
    assertTrue(values.contains("Medium"));
    assertTrue(values.contains("Large"));

    verify(attributeRepository).save(any(Attribute.class));
  }

  @Test
  void createAttributeWithValues_EmptyValues_CreatesAttributeWithNoValues() {
    // Given
    CreateAttributeDTO dto = new CreateAttributeDTO();
    dto.setName("Size");
    dto.setValues(Collections.emptyList());

    Attribute createdAttribute = new Attribute();
    createdAttribute.setId(2L);
    createdAttribute.setName("Size");
    createdAttribute.setValues(Collections.emptySet());

    when(attributeRepository.save(any(Attribute.class))).thenReturn(createdAttribute);

    // When
    AttributeDTO result = attributeService.createAttributeWithValues(dto);

    // Then
    assertNotNull(result);
    assertEquals("Size", result.getName());
    assertTrue(result.getValues().isEmpty());
    verify(attributeRepository).save(any(Attribute.class));
  }
}
