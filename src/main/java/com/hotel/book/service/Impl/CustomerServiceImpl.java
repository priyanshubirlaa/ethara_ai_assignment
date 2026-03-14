package com.hotel.book.service.Impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotel.book.dto.CustomerRequestDTO;
import com.hotel.book.dto.CustomerResponseDTO;
import com.hotel.book.entity.Customer;
import com.hotel.book.entity.Role;
import com.hotel.book.repository.CustomerRepository;
import com.hotel.book.service.CustomerService;
import com.hotel.book.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {

    Optional<Customer> existingCustomer = repository.findFirstByPhone(request.getPhone());

    if (existingCustomer.isPresent()) {
        return mapToResponse(existingCustomer.get());
    }

    Customer customer = Customer.builder()
            .name(request.getName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .build();

    Customer saved = repository.save(customer);

    return mapToResponse(saved);
}

@Override
public CustomerResponseDTO createCustomerForce(CustomerRequestDTO request) {

    Customer customer = Customer.builder()
            .name(request.getName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .build();

    Customer saved = repository.save(customer);

    return mapToResponse(saved);
}

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request) {

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        return mapToResponse(repository.save(customer));
    }

    private CustomerResponseDTO mapToResponse(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();
    }
}
