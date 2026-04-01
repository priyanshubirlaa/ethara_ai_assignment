package com.hotel.book.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.book.dto.RoomPriceUpdateRequestDTO;
import com.hotel.book.dto.RoomRequestDTO;
import com.hotel.book.dto.RoomResponseDTO;
import com.hotel.book.service.RoomService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponseDTO> addRoomToHotel(
            @PathVariable Long hotelId,
            @Validated @RequestBody RoomRequestDTO request) {
        RoomResponseDTO response = roomService.addRoomToHotel(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<RoomResponseDTO>> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RoomResponseDTO> result = roomService.getRoomsByHotel(hotelId, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{roomId}")
public ResponseEntity<RoomResponseDTO> getRoom(
        @PathVariable Long hotelId,
        @PathVariable Long roomId) {

    return ResponseEntity.ok(
            roomService.getRoomByHotelAndRoom(hotelId, roomId));
}

@GetMapping
public ResponseEntity<Page<RoomResponseDTO>> searchRooms(
        @PathVariable Long hotelId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

    Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    return ResponseEntity.ok(
            roomService.searchRooms(
                    hotelId, minPrice, maxPrice, checkIn, checkOut, pageable
            )
    );
}

@GetMapping("/dynamic-pricing")
public ResponseEntity<Page<RoomResponseDTO>> getDynamicPricedRooms(
        @PathVariable Long hotelId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

    Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    return ResponseEntity.ok(
            roomService.getDynamicPricedRooms(
                    hotelId, checkIn, checkOut, pageable
            )
    );
}


@PatchMapping("/{roomId}/price")
public ResponseEntity<String> updateRoomPrice(
        @PathVariable Long roomId,
        @RequestBody RoomPriceUpdateRequestDTO request) {

    roomService.updateRoomPrice(roomId, request.getPrice());

    return ResponseEntity.ok("Room price updated successfully");
}

}
