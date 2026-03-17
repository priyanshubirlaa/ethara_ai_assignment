package com.hotel.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hotel.book.entity.HotelReview;

public interface HotelReviewRepository extends JpaRepository<HotelReview, Long> {

    List<HotelReview> findByHotelId(Long hotelId);

    @Query("SELECT AVG(r.rating) FROM HotelReview r WHERE r.hotelId = :hotelId")
    Double getAverageRating(Long hotelId);
}