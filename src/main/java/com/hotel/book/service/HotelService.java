package com.hotel.book.service;

import org.springframework.data.domain.Pageable;

import com.hotel.book.dto.HotelRequestDTO;
import com.hotel.book.dto.HotelResponseDTO;
import com.hotel.book.dto.PageResponse;

public interface HotelService {

    HotelResponseDTO addHotel(HotelRequestDTO request);

    HotelResponseDTO getHotelById(Long id);

    PageResponse<HotelResponseDTO> getHotels(String city, Pageable pageable);
}

