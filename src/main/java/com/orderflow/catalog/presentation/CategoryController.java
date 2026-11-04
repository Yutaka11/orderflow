package com.orderflow.catalog.presentation;

import com.orderflow.catalog.application.CategoryService;
import com.orderflow.catalog.presentation.dto.CategoryResponse;
import com.orderflow.catalog.presentation.dto.CreateCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")

public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse create(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        return categoryService.create(request);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }
}