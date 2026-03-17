package com.hotel.book.service.Impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.book.dto.ReviewRequestDTO;
import com.hotel.book.entity.HotelReview;
import com.hotel.book.repository.HotelReviewRepository;
import com.hotel.book.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final HotelReviewRepository reviewRepository;

    @Override
    @Transactional
    public void addReview(ReviewRequestDTO request) {

        HotelReview review = new HotelReview();

        review.setHotelId(request.getHotelId());
        review.setRating(request.getRating());
        review.setReviewComment(request.getReviewComment());
        review.setReviewDate(LocalDate.now());

        reviewRepository.save(review);

        log.info("Review added successfully for hotelId={}", request.getHotelId());
    }

    @Override
    public List<HotelReview> getReviewsByHotel(Long hotelId) {

        List<HotelReview> reviews = reviewRepository.findByHotelId(hotelId);

        log.info("Fetched {} reviews for hotelId={}", reviews.size(), hotelId);

        return reviews;
    }

    @Override
    public Double getAverageRating(Long hotelId) {

        Double avgRating = reviewRepository.getAverageRating(hotelId);

        if (avgRating == null) {
            avgRating = 0.0;
        }

        log.info("Average rating for hotelId={} is {}", hotelId, avgRating);

        return avgRating;
    }
}