package com.ecommerce.repository;

import com.ecommerce.model.entity.Attribute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
  Optional<Attribute> findByName(String name);

  @Query(
      value =
          "SELECT a.id as id, a.name as name, STRING_AGG(av.value, ',') as values "
              + "FROM attributes a LEFT JOIN attribute_values av ON a.id = av.attribute_id "
              + "GROUP BY a.id, a.name",
      nativeQuery = true)
  List<Object[]> findAllWithValues();

  @Query(
      "SELECT a.id, a.name, av.value, av.id FROM Attribute a "
          + "LEFT JOIN a.values av "
          + "WHERE a.id IN :attributeIds")
  List<Object[]> findAttributesWithValuesByIds(@Param("attributeIds") List<Long> attributeIds);
}
