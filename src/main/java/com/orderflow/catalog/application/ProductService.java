package com.orderflow.catalog.application;

import com.orderflow.catalog.domain.Category;
import com.orderflow.catalog.domain.Product;
import com.orderflow.catalog.infrastructure.CategoryRepository;
import com.orderflow.catalog.infrastructure.ProductRepository;
import com.orderflow.catalog.presentation.dto.CreateProductRequest;
import com.orderflow.catalog.presentation.dto.ProductResponse;
import com.orderflow.shared.exception.ResourceConflictException;
import com.orderflow.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderflow.catalog.infrastructure.specification.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String sku = request.sku().trim();

        if (productRepository.existsBySku(sku)) {
            throw new ResourceConflictException(
                    "A product with this SKU already exists"
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found: " + request.categoryId()
                ));

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setSku(sku);
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setActive(true);

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + id
                ));

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive()
        );
    }
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(
            String name,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            Sort.Direction direction
    ) {
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        if (!List.of("name", "price", "createdAt").contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field");
        }

        Specification<Product> specification = Specification
                .where(ProductSpecifications.hasName(name))
                .and(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.hasMinimumPrice(minPrice))
                .and(ProductSpecifications.hasMaximumPrice(maxPrice));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );

        return productRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }

}