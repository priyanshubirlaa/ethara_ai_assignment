package com.hotel.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.BookingRequestDTO;
import com.hotel.book.dto.BookingResponseDTO;
import com.hotel.book.entity.BookingStatus;
import com.hotel.book.exception.BusinessException;
import com.hotel.book.exception.ResourceNotFoundException;
import com.hotel.book.security.JwtFilter;
import com.hotel.book.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtFilter jwtFilter;

    private BookingRequestDTO bookingRequestDTO;
    private BookingResponseDTO bookingResponseDTO;

    @BeforeEach
    void setUp() {
        bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setCustomerId(1L);
        bookingRequestDTO.setHotelId(1L);
        bookingRequestDTO.setRoomId(1L);
        bookingRequestDTO.setCheckInDate(LocalDate.now().plusDays(1));
        bookingRequestDTO.setCheckOutDate(LocalDate.now().plusDays(3));

        bookingResponseDTO = BookingResponseDTO.builder()
                .bookingId(1L)
                .customerName("John Doe")
                .hotelName("Grand Hotel")
                .roomType("Deluxe")
                .status(BookingStatus.CONFIRMED)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .build();
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testCreateBooking_Success() throws Exception {
        when(bookingService.createBooking(any(BookingRequestDTO.class))).thenReturn(bookingResponseDTO);

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(1L))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testCreateBooking_ValidationError() throws Exception {
        bookingRequestDTO.setCheckInDate(null); // Invalid: null check-in date

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testCreateBooking_BusinessException() throws Exception {
        when(bookingService.createBooking(any(BookingRequestDTO.class)))
                .thenThrow(new BusinessException("Check-out date must be after check-in date"));

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-out date must be after check-in date"));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testGetBooking_Success() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(bookingResponseDTO);

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(1L))
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testGetBooking_NotFound() throws Exception {
        when(bookingService.getBookingById(999L))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/api/bookings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found"));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testCancelBooking_Success() throws Exception {
        BookingResponseDTO cancelledBooking = BookingResponseDTO.builder()
                .bookingId(1L)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingService.cancelBooking(1L)).thenReturn(cancelledBooking);

        mockMvc.perform(put("/api/bookings/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testGetBookingsByStatus_Success() throws Exception {
        List<BookingResponseDTO> bookings = Arrays.asList(bookingResponseDTO);
        Page<BookingResponseDTO> page = new PageImpl<>(bookings, PageRequest.of(0, 10), 1L);

        when(bookingService.getBookingsByStatus(eq(BookingStatus.CONFIRMED), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/bookings/status/CONFIRMED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testGetBookingsByStatus_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/bookings/status/INVALID")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid booking status")));
    }

    @Test
    @WithMockUser(roles = {"STAFF", "ADMIN"})
    void testSearchBookings_Success() throws Exception {
        List<BookingResponseDTO> bookings = Arrays.asList(bookingResponseDTO);
        Page<BookingResponseDTO> page = new PageImpl<>(bookings, PageRequest.of(0, 10), 1L);

        when(bookingService.searchBookings(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/bookings")
                        .param("status", "CONFIRMED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testCreateBooking_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}
