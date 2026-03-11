package com.hotel.book.service.Impl;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.book.dto.BookingRequestDTO;
import com.hotel.book.dto.BookingResponseDTO;
import com.hotel.book.entity.*;
import com.hotel.book.exception.BusinessException;
import com.hotel.book.exception.ResourceNotFoundException;
import com.hotel.book.repository.*;
import com.hotel.book.service.BookingService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    private final Counter bookingSuccessCounter;
    private final Counter bookingFailureCounter;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            MeterRegistry meterRegistry) {

        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;

        this.bookingSuccessCounter =
                meterRegistry.counter("booking.success.count");

        this.bookingFailureCounter =
                meterRegistry.counter("booking.failure.count");
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {

        try {

            if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
                throw new BusinessException("Check-out date must be after check-in date");
            }

            if (request.getCheckInDate().isBefore(LocalDate.now())) {
                throw new BusinessException("Check-in date cannot be in the past");
            }

            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

            Room room = roomRepository.findByIdAndHotelId(
                    request.getRoomId(),
                    request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                    room.getId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );

            if (!overlapping.isEmpty()) {
                throw new BusinessException("Room already booked for selected dates");
            }

            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setHotel(hotel);
            booking.setRoom(room);
            booking.setCheckInDate(request.getCheckInDate());
            booking.setCheckOutDate(request.getCheckOutDate());
            booking.setStatus(BookingStatus.CONFIRMED);

            Booking saved = bookingRepository.save(booking);

            bookingSuccessCounter.increment();

            MDC.put("status", "201");
            log.info("Booking created successfully: bookingId={} customerId={} hotelId={} roomId={}",
                    saved.getId(),
                    customer.getId(),
                    hotel.getId(),
                    room.getId());

            return mapToResponse(saved);

        } catch (Exception e) {

            bookingFailureCounter.increment();
            throw e;
        }
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        MDC.put("status", "200");
        log.info("Booking cancelled successfully: bookingId={} customerId={} hotelId={} roomId={}",
                saved.getId(),
                saved.getCustomer().getId(),
                saved.getHotel().getId(),
                saved.getRoom().getId());

        return mapToResponse(saved);
    }

    @Override
    public Page<BookingResponseDTO> getBookingsByStatus(
            BookingStatus status,
            Pageable pageable) {

        return bookingRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<BookingResponseDTO> searchBookings(
            BookingStatus status,
            Long customerId,
            Long hotelId,
            Pageable pageable) {

        Page<Booking> page;

        if (status != null) {
            page = bookingRepository.findByStatus(status, pageable);

        } else if (customerId != null) {
            page = bookingRepository.findByCustomerId(customerId, pageable);

        } else if (hotelId != null) {
            page = bookingRepository.findByHotelId(hotelId, pageable);

        } else {
            page = bookingRepository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    private BookingResponseDTO mapToResponse(Booking booking) {

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .customerName(booking.getCustomer().getName())
                .hotelName(booking.getHotel().getName())
                .roomType(booking.getRoom().getType())
                .status(booking.getStatus())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .build();
    }
}