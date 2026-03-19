package com.hotel.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.book.entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Page<Hotel> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    @Query("""
        SELECT DISTINCT h FROM Hotel h
        WHERE (:city IS NULL OR LOWER(h.location) LIKE LOWER(CONCAT('%', :city, '%')))
        AND EXISTS (
            SELECT r FROM Room r
            WHERE r.hotel = h
            AND r.id NOT IN (
                SELECT b.room.id FROM Booking b
                WHERE b.status = 'CONFIRMED'
                AND b.checkInDate < :checkOut
                AND b.checkOutDate > :checkIn
            )
        )
    """)
    Page<Hotel> findAvailableHotels(
            @Param("city") String city,
            @Param("checkIn") java.time.LocalDate checkIn,
            @Param("checkOut") java.time.LocalDate checkOut,
            Pageable pageable
    );
}
