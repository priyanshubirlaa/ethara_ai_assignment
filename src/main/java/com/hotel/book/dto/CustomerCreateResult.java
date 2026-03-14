package com.hotel.book.dto;

public class CustomerCreateResult {

    private boolean alreadyExists;
    private CustomerResponseDTO customer;

    public CustomerCreateResult(boolean alreadyExists, CustomerResponseDTO customer) {
        this.alreadyExists = alreadyExists;
        this.customer = customer;
    }

    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    public CustomerResponseDTO getCustomer() {
        return customer;
    }
}