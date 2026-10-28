package com.orderflow.catalog.infrastructure;

import com.orderflow.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {
    boolean existsBySku(String sku);
}