package com.hotel.book.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.hotel.book.dto.ReviewRequestDTO;
import com.hotel.book.entity.HotelReview;
import com.hotel.book.service.GeminiService;
import com.hotel.book.service.ReviewService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;
    private final GeminiService geminiService;



    @PostMapping
    public ResponseEntity<String> addReview(@Valid @RequestBody ReviewRequestDTO request) {

        reviewService.addReview(request);

        return ResponseEntity.ok("Review added successfully");
    }

    @GetMapping("/hotel/{hotelId}")
    public List<HotelReview> getReviews(@PathVariable Long hotelId) {

        return reviewService.getReviewsByHotel(hotelId);
    }

    @GetMapping("/hotel/{hotelId}/average")
    public Double getAverageRating(@PathVariable Long hotelId) {

        return reviewService.getAverageRating(hotelId);
    }

    @GetMapping("/hotel/{hotelId}/summary")
public String summarizeReviews(@PathVariable Long hotelId) {

    List<HotelReview> reviews = reviewService.getReviewsByHotel(hotelId);

    List<String> comments = reviews.stream()
            .map(HotelReview::getReviewComment)
            .toList();

    return geminiService.summarizeReviews(comments);
}
}
