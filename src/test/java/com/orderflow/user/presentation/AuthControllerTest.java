
package com.orderflow.user.presentation;

import com.orderflow.shared.exception.ResourceConflictException;
import com.orderflow.user.application.RegisterUserService;
import com.orderflow.user.application.LoginService;
import com.orderflow.user.domain.Role;
import com.orderflow.user.infrastructure.security.JwtService;
import com.orderflow.user.infrastructure.UserRepository;
import com.orderflow.user.presentation.dto.AuthResponse;
import com.orderflow.user.presentation.dto.UserResponse;
import com.orderflow.user.infrastructure.security.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserService registerUserService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();

        when(registerUserService.execute(any()))
                .thenReturn(new UserResponse(
                        userId,
                        "Henrique",
                        "henrique@email.com",
                        Role.CUSTOMER
                ));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Henrique",
                                    "email": "henrique@email.com",
                                    "password": "Password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Henrique"))
                .andExpect(jsonPath("$.email").value("henrique@email.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectRegistrationWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "invalid-email",
                                    "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registerUserService);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(loginService.execute(any()))
                .thenReturn(new AuthResponse(
                        "mock-jwt-token",
                        "Bearer"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "henrique@email.com",
                                    "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRejectLoginWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "invalid-email",
                                    "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loginService);
    }
    @Test
    void shouldRejectRegistrationWithDuplicateEmail() throws Exception {
        when(registerUserService.execute(any()))
                .thenThrow(new ResourceConflictException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Henrique",
                                "email": "henrique@email.com",
                                "password": "Password123"
                            }
                            """))
                .andExpect(status().isConflict());
    }
    @Test
    void shouldRejectLoginWithInvalidCredentials() throws Exception {
        when(loginService.execute(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "henrique@email.com",
                                "password": "WrongPassword123"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }
}
