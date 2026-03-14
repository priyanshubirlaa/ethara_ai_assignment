package com.hotel.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.CustomerRequestDTO;
import com.hotel.book.dto.CustomerResponseDTO;
import com.hotel.book.exception.ResourceNotFoundException;
import com.hotel.book.security.JwtFilter;
import com.hotel.book.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JwtFilter jwtFilter;

    private CustomerRequestDTO customerRequestDTO;
    private CustomerResponseDTO customerResponseDTO;

    @BeforeEach
    void setUp() {

        customerRequestDTO = new CustomerRequestDTO();
        customerRequestDTO.setName("John Doe");
        customerRequestDTO.setEmail("john.doe@example.com");
        customerRequestDTO.setPhone("1234567890");

        customerResponseDTO = CustomerResponseDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCustomer_Success() throws Exception {

        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenReturn(customerResponseDTO);

        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCustomer_Success() throws Exception {

        when(customerService.getCustomerById(1L))
                .thenReturn(customerResponseDTO);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCustomer_NotFound() throws Exception {

        when(customerService.getCustomerById(999L))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void testCreateCustomer_Unauthenticated() throws Exception {

        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}