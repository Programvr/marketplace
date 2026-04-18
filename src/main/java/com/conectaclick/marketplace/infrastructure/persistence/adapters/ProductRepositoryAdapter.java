package com.conectaclick.marketplace.infrastructure.persistence.adapters;

import com.conectaclick.marketplace.domain.model.Product;
import com.conectaclick.marketplace.domain.ports.outbound.ProductRepositoryPort;
import com.conectaclick.marketplace.infrastructure.persistence.entities.ProductEntity;
import com.conectaclick.marketplace.infrastructure.persistence.mappers.ProductPersistenceMapper;
import com.conectaclick.marketplace.infrastructure.persistence.repositories.JpaProductRepository;
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
