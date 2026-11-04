
package com.orderflow.catalog.presentation;

import com.orderflow.catalog.application.ProductService;
import com.orderflow.catalog.presentation.dto.ProductResponse;
import com.orderflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.orderflow.user.infrastructure.security.SecurityConfig;
import com.orderflow.user.infrastructure.security.JwtService;
import com.orderflow.user.infrastructure.UserRepository;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldAllowPublicAccessToGetProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAnonymousUserWhenCreatingProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoryId": "%s",
                                "name": "Keyboard",
                                "description": "Mechanical keyboard",
                                "sku": "KEY-001",
                                "price": 199.90,
                                "stockQuantity": 10
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCustomerWhenCreatingProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(user("customer").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "categoryId": "%s",
                                    "name": "Keyboard",
                                    "description": "Mechanical keyboard",
                                    "sku": "KEY-001",
                                    "price": 199.90,
                                    "stockQuantity": 10
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCreateProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(productService.create(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ProductResponse(
                        productId,
                        categoryId,
                        "Keyboard",
                        "Mechanical keyboard",
                        "KEY-001",
                        new BigDecimal("199.90"),
                        10,
                        true
                ));

        mockMvc.perform(post("/api/products")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "categoryId": "%s",
                                    "name": "Keyboard",
                                    "description": "Mechanical keyboard",
                                    "sku": "KEY-001",
                                    "price": 199.90,
                                    "stockQuantity": 10
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.sku").value("KEY-001"));
    }
}
