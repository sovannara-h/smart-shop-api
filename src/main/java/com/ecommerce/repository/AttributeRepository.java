package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.entity.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByName(String name);
}
