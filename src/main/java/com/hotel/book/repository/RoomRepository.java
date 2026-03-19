package com.hotel.book.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.book.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByHotelId(Long hotelId, Pageable pageable);

    Page<Room> findByHotelIdAndPriceBetween(
            Long hotelId,
            Double minPrice,
            Double maxPrice,
            Pageable pageable
    );

    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.id = :hotelId
        AND r.id NOT IN (
            SELECT b.room.id FROM Booking b
            WHERE b.status = 'CONFIRMED'
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
        )
    """)
    Page<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            Pageable pageable
    );

    Optional<Room> findByIdAndHotelId(Long roomId, Long hotelId);

    long countByHotelId(Long hotelId);
}
