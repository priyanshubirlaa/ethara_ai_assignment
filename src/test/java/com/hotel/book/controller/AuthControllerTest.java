package com.hotel.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.LoginRequest;
import com.hotel.book.dto.UserRegisterRequest;
import com.hotel.book.entity.Role;
import com.hotel.book.entity.User;
import com.hotel.book.repository.CustomerRepository;
import com.hotel.book.repository.UserRepository;
import com.hotel.book.security.JwtFilter;
import com.hotel.book.security.JwtUtil;
import com.hotel.book.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private JwtFilter jwtFilter;

    private Counter authSuccessCounter;
    private Counter authFailureCounter;

    private LoginRequest loginRequest;
    private UserRegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {

        authSuccessCounter = Mockito.mock(Counter.class);
        authFailureCounter = Mockito.mock(Counter.class);

        when(meterRegistry.counter("auth.success.count")).thenReturn(authSuccessCounter);
        when(meterRegistry.counter("auth.failure.count")).thenReturn(authFailureCounter);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@hotel.com");
        loginRequest.setPassword("password123");

        registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("newuser@hotel.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.ADMIN);

        user = new User();
        user.setId(1L);
        user.setEmail("admin@hotel.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);
    }

    @Test
    void testLogin_Success() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByEmail("admin@hotel.com"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("admin@hotel.com", "ADMIN"))
                .thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authSuccessCounter, times(1)).increment();
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(authFailureCounter, times(1)).increment();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegister_Success() throws Exception {

        doNothing().when(userService).registerUser(any(UserRegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User created successfully"));
    }

    @Test
    void testRegister_Unauthenticated() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isUnauthorized());
    }
}