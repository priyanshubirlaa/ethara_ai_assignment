package com.hotel.book.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long id;
    private Long hotelId;
    private Integer rating;
    private String reviewComment;
    private LocalDate reviewDate;
}