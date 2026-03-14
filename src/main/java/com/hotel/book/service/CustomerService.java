package com.hotel.book.service;

import com.hotel.book.dto.CustomerRequestDTO;
import com.hotel.book.dto.CustomerResponseDTO;

public interface CustomerService {

    CustomerResponseDTO createCustomer(CustomerRequestDTO request);

    CustomerResponseDTO createCustomerForce(CustomerRequestDTO request);


    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request);
}
