package com.hotel.book.service;

import com.hotel.book.dto.RoomRequestDTO;
import com.hotel.book.dto.RoomResponseDTO;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {

    RoomResponseDTO addRoomToHotel(Long hotelId, RoomRequestDTO request);

    Page<RoomResponseDTO> getRoomsByHotel(Long hotelId, Pageable pageable);

    RoomResponseDTO getRoomByHotelAndRoom(Long hotelId, Long roomId);

    void updateRoomPrice(Long roomId, Double price);

    Page<RoomResponseDTO> searchRooms(
        Long hotelId,
        Double minPrice,
        Double maxPrice,
        LocalDate checkIn,
        LocalDate checkOut,
        Pageable pageable
);
}
