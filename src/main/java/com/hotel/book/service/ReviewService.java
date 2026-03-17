package com.hotel.book.service;

import java.util.List;

import com.hotel.book.dto.ReviewRequestDTO;
import com.hotel.book.entity.HotelReview;

public interface ReviewService {

    void addReview(ReviewRequestDTO request);

    List<HotelReview> getReviewsByHotel(Long hotelId);

    Double getAverageRating(Long hotelId);

}