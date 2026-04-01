package com.ConectaClick.marketplace.infrastructure.persistence.repositories;

import com.ConectaClick.marketplace.infrastructure.persistence.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {
}