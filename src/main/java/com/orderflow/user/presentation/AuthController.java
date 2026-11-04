package com.orderflow.user.presentation;

import com.orderflow.user.application.LoginService;
import com.orderflow.user.application.RegisterUserService;
import com.orderflow.user.presentation.dto.AuthResponse;
import com.orderflow.user.presentation.dto.LoginRequest;
import com.orderflow.user.presentation.dto.RegisterUserRequest;
import com.orderflow.user.presentation.dto.UserResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginService loginService;

    public AuthController(
            RegisterUserService registerUserService,
            LoginService loginService
    ) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        UserResponse response = registerUserService.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = loginService.execute(request);

        return ResponseEntity.ok(response);
    }
}