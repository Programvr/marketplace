package com.ConectaClick.marketplace.domain.ports.outbound;

import com.ConectaClick.marketplace.domain.model.Product;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    boolean existsById(Long id);
}