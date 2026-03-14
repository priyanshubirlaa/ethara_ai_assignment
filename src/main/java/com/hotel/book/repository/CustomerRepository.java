package com.hotel.book.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hotel.book.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findFirstByPhone(String phone);
}
