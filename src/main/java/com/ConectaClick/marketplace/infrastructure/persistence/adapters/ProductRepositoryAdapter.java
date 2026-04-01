package com.ConectaClick.marketplace.infrastructure.persistence.adapters;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.ports.outbound.ProductRepositoryPort;
import com.ConectaClick.marketplace.infrastructure.persistence.entities.ProductEntity;
import com.ConectaClick.marketplace.infrastructure.persistence.mappers.ProductPersistenceMapper;
import com.ConectaClick.marketplace.infrastructure.persistence.repositories.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final JpaProductRepository jpaProductRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity savedEntity = jpaProductRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaProductRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaProductRepository.existsById(id);
    }
}