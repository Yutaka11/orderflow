
package com.orderflow.order.presentation;

import com.orderflow.order.application.OrderService;
import com.orderflow.order.domain.OrderStatus;
import com.orderflow.order.presentation.dto.OrderResponse;
import com.orderflow.shared.exception.ResourceNotFoundException;
import com.orderflow.user.infrastructure.UserRepository;
import com.orderflow.user.infrastructure.security.JwtService;
import com.orderflow.user.infrastructure.security.SecurityConfig;

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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                orderId,
                OrderStatus.PENDING,
                new BigDecimal("100.00"),
                List.of(),
                null
        );

        when(orderService.createOrder(
                eq("henrique@email.com"),
                any()
        )).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .with(user("henrique@email.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "items": [
                                    {
                                        "productId": "%s",
                                        "quantity": 2
                                    }
                                ]
                            }
                            """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }


    @Test
    void shouldReturnAuthenticatedUserOrders() throws Exception {

        UUID firstOrderId = UUID.randomUUID();
        UUID secondOrderId = UUID.randomUUID();

        List<OrderResponse> orders = List.of(
                new OrderResponse(
                        firstOrderId,
                        OrderStatus.PENDING,
                        new BigDecimal("100.00"),
                        List.of(),
                        null
                ),
                new OrderResponse(
                        secondOrderId,
                        OrderStatus.PAID,
                        new BigDecimal("200.00"),
                        List.of(),
                        null
                )
        );

        when(orderService.findAllByUser("henrique@email.com"))
                .thenReturn(orders);

        mockMvc.perform(get("/api/orders")
                        .with(user("henrique@email.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("PAID"));

        verify(orderService).findAllByUser("henrique@email.com");
    }


    @Test
    void shouldReturnOrderById() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                orderId,
                OrderStatus.PENDING,
                new BigDecimal("150.00"),
                List.of(),
                null
        );

        when(orderService.findById("henrique@email.com", orderId))
                .thenReturn(response);

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .with(user("henrique@email.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(150.00));

        verify(orderService).findById("henrique@email.com", orderId);
    }


    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        UUID orderId = UUID.randomUUID();

        when(orderService.findById("henrique@email.com", orderId))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .with(user("henrique@email.com").roles("CUSTOMER")))
                .andExpect(status().isNotFound());

        verify(orderService).findById("henrique@email.com", orderId);
    }


    @Test
    void shouldRejectUnauthenticatedUser() throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "items": []
                        }
                        """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }


    @Test
    void shouldRejectOrderWithEmptyItems() throws Exception {

        mockMvc.perform(post("/api/orders")
                        .with(user("henrique@email.com").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "items": []
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

}
