package com.ecommerce.repository;

import com.ecommerce.model.entity.Attribute;
import com.ecommerce.model.entity.AttributeValue;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
  Optional<AttributeValue> findByAttributeAndValue(Attribute attribute, String value);
}
