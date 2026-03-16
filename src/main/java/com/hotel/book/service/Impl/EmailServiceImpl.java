package com.hotel.book.service.Impl;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hotel.book.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendBookingConfirmation(String toEmail,
                                        String hotelName,
                                        String roomType,
                                        String checkIn,
                                        String checkOut) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Booking Confirmation");

        message.setText(
                "Your booking has been confirmed.\n\n" +
                "Hotel: " + hotelName + "\n" +
                "Room: " + roomType + "\n" +
                "Check-in: " + checkIn + "\n" +
                "Check-out: " + checkOut + "\n\n" +
                "Thank you for choosing our hotel."
        );

        mailSender.send(message);
    }

    @Override
    public void sendBookingCancellation(String toEmail,
                                        String hotelName,
                                        String roomType,
                                        String checkIn,
                                        String checkOut) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Booking Cancelled");

        message.setText(
                "Your booking has been cancelled.\n\n" +
                "Hotel: " + hotelName + "\n" +
                "Room: " + roomType + "\n" +
                "Check-in: " + checkIn + "\n" +
                "Check-out: " + checkOut + "\n\n" +
                "We hope to serve you again."
        );

        mailSender.send(message);
    }
}