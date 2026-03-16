package com.hotel.book.service;


public interface EmailService {

    void sendBookingConfirmation(String toEmail, String hotelName,
                                 String roomType, String checkIn,
                                 String checkOut);

    void sendBookingCancellation(String toEmail, String hotelName,
                                 String roomType, String checkIn,
                                 String checkOut);
}