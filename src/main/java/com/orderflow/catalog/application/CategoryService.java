package com.orderflow.catalog.application;

import com.orderflow.catalog.domain.Category;
import com.orderflow.catalog.infrastructure.CategoryRepository;
import com.orderflow.catalog.presentation.dto.CategoryResponse;
import com.orderflow.catalog.presentation.dto.CreateCategoryRequest;
import com.orderflow.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        String name = request.name().trim();
        String slug = request.slug().trim().toLowerCase();

        if (categoryRepository.existsBySlug(slug)) {
            throw new ResourceConflictException(
                    "A category with this slug already exists"
            );
        }

        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setActive(true);
        category.setCreatedAt(Instant.now());

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.isActive()
        );
    }
}