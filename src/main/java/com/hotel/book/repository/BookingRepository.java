package com.hotel.book.repository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.hotel.book.entity.Booking;
import com.hotel.book.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status = 'CONFIRMED'
        AND b.checkInDate < :checkOutDate
        AND b.checkOutDate > :checkInDate
    """)
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    //Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);

    Page<Booking> findByHotelId(Long hotelId, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT b.room.id) FROM Booking b
        WHERE b.hotel.id = :hotelId
        AND b.status = 'CONFIRMED'
        AND b.checkInDate < :checkOutDate
        AND b.checkOutDate > :checkInDate
    """)
    long countBookedRoomsForHotel(
            @Param("hotelId") Long hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
