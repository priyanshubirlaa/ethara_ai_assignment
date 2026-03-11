package com.hotel.book.service.Impl;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hotel.book.dto.RoomRequestDTO;
import com.hotel.book.dto.RoomResponseDTO;
import com.hotel.book.entity.Hotel;
import com.hotel.book.entity.Room;
import com.hotel.book.exception.ResourceNotFoundException;
import com.hotel.book.repository.HotelRepository;
import com.hotel.book.repository.RoomRepository;
import com.hotel.book.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @CacheEvict(value = "rooms", key = "#hotelId + '-' + #pageable.pageNumber", allEntries = true)
    @Override
    public RoomResponseDTO addRoomToHotel(Long hotelId, RoomRequestDTO request) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Room room = new Room();
        room.setType(request.getType());
        room.setPrice(request.getPrice());
        room.setHotel(hotel);

        Room saved = roomRepository.save(room);
        return mapToResponse(saved);
    }

    @Cacheable(value = "rooms", key = "#hotelId + '-' + #pageable.pageNumber")
    @Override
    public Page<RoomResponseDTO> getRoomsByHotel(Long hotelId, Pageable pageable) {
    
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found");
        }
    
        Page<Room> roomsPage = roomRepository.findByHotelId(hotelId, pageable);
    
        return roomsPage.map(this::mapToResponse);
    }

    @Override
    public RoomResponseDTO getRoomByHotelAndRoom(Long hotelId, Long roomId) {

        Room room = roomRepository
                .findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found for this hotel"));

        return mapToResponse(room);
    }

    private RoomResponseDTO mapToResponse(Room room) {
        return RoomResponseDTO.builder()
                .id(room.getId())
                .type(room.getType())
                .price(room.getPrice())
                .build();
    }

    @Override
public Page<RoomResponseDTO> searchRooms(
        Long hotelId,
        Double minPrice,
        Double maxPrice,
        LocalDate checkIn,
        LocalDate checkOut,
        Pageable pageable) {

    if (!hotelRepository.existsById(hotelId)) {
        throw new ResourceNotFoundException("Hotel not found");
    }

    Page<Room> page;

    if (checkIn != null && checkOut != null) {
        page = roomRepository.findAvailableRooms(hotelId, checkIn, checkOut, pageable);

    } else if (minPrice != null && maxPrice != null) {
        page = roomRepository.findByHotelIdAndPriceBetween(
                hotelId, minPrice, maxPrice, pageable);

    } else {
        page = roomRepository.findByHotelId(hotelId, pageable);
    }

    return page.map(this::mapToResponse);
}
}
