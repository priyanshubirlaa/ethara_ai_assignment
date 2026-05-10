package com.hotel.book.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hotel.book.dto.ApiResponse;
import com.hotel.book.dto.CustomerRequestDTO;
import com.hotel.book.dto.CustomerResponseDTO;
import com.hotel.book.entity.Customer;
import com.hotel.book.repository.CustomerRepository;
import com.hotel.book.service.CustomerService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository repository;

    @PostMapping
public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomer(
        @Validated @RequestBody CustomerRequestDTO request) {

    Optional<Customer> existing = repository.findFirstByPhone(request.getPhone());

    if (existing.isPresent()) {

        CustomerResponseDTO response = CustomerResponseDTO.builder()
                .id(existing.get().getId())
                .name(existing.get().getName())
                .email(existing.get().getEmail())
                .phone(existing.get().getPhone())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("Customer already exists with this phone number", response));
    }

    CustomerResponseDTO response = customerService.createCustomer(request);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Customer created successfully", response));
}

    @PostMapping("/force")
    public ResponseEntity<CustomerResponseDTO> createCustomerForce(
            @Validated @RequestBody CustomerRequestDTO request) {

        CustomerResponseDTO response = customerService.createCustomerForce(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Validated @RequestBody CustomerRequestDTO request) {

        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }
}
