package com.hotel.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hotel.book.dto.BookingRequestDTO;
import com.hotel.book.dto.BookingResponseDTO;
import com.hotel.book.entity.BookingStatus;

public interface BookingService {

    BookingResponseDTO createBooking(BookingRequestDTO request);
    

    BookingResponseDTO getBookingById(Long id);

    BookingResponseDTO cancelBooking(Long id);

    Page<BookingResponseDTO> getBookingsByStatus(BookingStatus status, Pageable pageable);

    Page<BookingResponseDTO> searchBookings(
        BookingStatus status,
        Long customerId,
        Long hotelId,
        Pageable pageable
);


}

