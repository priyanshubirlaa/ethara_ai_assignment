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
import com.hotel.book.entity.RoomType;
import com.hotel.book.exception.ResourceNotFoundException;
import com.hotel.book.repository.HotelRepository;
import com.hotel.book.repository.RoomRepository;
import com.hotel.book.repository.BookingRepository;
import com.hotel.book.service.AuditLogService;
import com.hotel.book.service.PricingService;
import com.hotel.book.service.RoomService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final AuditLogService auditLogService;
    private final BookingRepository bookingRepository;
    private final PricingService pricingService;

    @CacheEvict(value = "rooms", key = "#hotelId + '-' + #pageable.pageNumber", allEntries = true)
    @Override
    public RoomResponseDTO addRoomToHotel(Long hotelId, RoomRequestDTO request) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Room room = new Room();

        RoomType roomType = RoomType.fromDisplayName(request.getType());
        if (roomType == null) {
            throw new com.hotel.book.exception.BusinessException(
                    "Invalid room type. Allowed values are: Standard Room, Superior Room, Deluxe Room, Executive Suite");
        }
        room.setType(roomType);
        room.setPrice(request.getPrice());
        room.setHotel(hotel);

        Room saved = roomRepository.save(room);
        RoomResponseDTO response = mapToResponse(saved);

        auditLogService.log(
                "ROOM_PRICE_UPDATED",
                "ROOM",
                saved.getId(),
                "Room created/updated with price=" + saved.getPrice()
        );

        return response;
    }

    @Cacheable(value = "rooms", key = "#hotelId + '-' + #pageable.pageNumber")
    @Override
    public Page<RoomResponseDTO> getRoomsByHotel(Long hotelId, Pageable pageable) {
    
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found");
        }
    
        Page<Room> roomsPage = roomRepository.findByHotelId(hotelId, pageable);

        // No date range here, so we keep base price (no dynamic adjustment)
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
                .type(room.getType().getDisplayName())
                .price(room.getPrice())
                .build();
    }

    @Override
@Transactional
public void updateRoomPrice(Long roomId, Double price) {

    Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    room.setPrice(price);

    roomRepository.save(room);

    log.info("Room price updated successfully: roomId={} newPrice={}", roomId, price);
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

        // Apply dynamic pricing only when a date range is provided
        if (checkIn != null && checkOut != null) {
            long totalRooms = roomRepository.countByHotelId(hotelId);
            long bookedRooms = bookingRepository.countBookedRoomsForHotel(hotelId, checkIn, checkOut);
            double occupancyRate = (totalRooms > 0) ? (double) bookedRooms / totalRooms : 0.0;

            return page.map(room -> {
                double adjustedPrice = pricingService.applyDynamicPricing(
                        room.getPrice(), occupancyRate);
                return RoomResponseDTO.builder()
                        .id(room.getId())
                        .type(room.getType().getDisplayName())
                        .price(adjustedPrice)
                        .build();
            });
        }

        // No date range: return base price
        return page.map(this::mapToResponse);
    }
}
