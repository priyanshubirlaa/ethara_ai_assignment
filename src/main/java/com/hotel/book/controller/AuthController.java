package com.hotel.book.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;

import com.hotel.book.repository.CustomerRepository;
import com.hotel.book.repository.UserRepository;
import com.hotel.book.entity.User;
import com.hotel.book.security.JwtUtil;
import com.hotel.book.service.UserService;
import com.hotel.book.dto.LoginRequest;
import com.hotel.book.dto.UserRegisterRequest;
import com.hotel.book.dto.AuthResponse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;

    public AuthController(
            CustomerRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            UserService userService,
            MeterRegistry meterRegistry) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userService = userService;

        this.authSuccessCounter =
                meterRegistry.counter("auth.success.count");

        this.authFailureCounter =
                meterRegistry.counter("auth.failure.count");
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> register(
            @Validated @RequestBody UserRegisterRequest request) {

        userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User created successfully");
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        try {

            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Invalid credentials"));

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name());

            authSuccessCounter.increment();

            return new AuthResponse(token, user.getRole().name());

        } catch (Exception e) {

            authFailureCounter.increment();
            throw e;
        }
    }
}
