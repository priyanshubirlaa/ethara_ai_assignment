package com.hotel.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.HotelRequestDTO;
import com.hotel.book.dto.HotelResponseDTO;
import com.hotel.book.dto.PageResponse;
import com.hotel.book.security.JwtFilter;
import com.hotel.book.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private JwtFilter jwtFilter;

    private HotelRequestDTO hotelRequestDTO;
    private HotelResponseDTO hotelResponseDTO;
    private PageResponse<HotelResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {

        hotelRequestDTO = new HotelRequestDTO();
        hotelRequestDTO.setName("Grand Hotel");
        hotelRequestDTO.setLocation("New York");

        hotelResponseDTO = HotelResponseDTO.builder()
                .id(1L)
                .name("Grand Hotel")
                .location("New York")
                .build();

        pageResponse = new PageResponse<>(List.of(hotelResponseDTO),0,10,1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddHotel_Success() throws Exception {

        when(hotelService.addHotel(any())).thenReturn(hotelResponseDTO);

        mockMvc.perform(post("/api/hotels")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHotels_Success() throws Exception {

        when(hotelService.getHotels(anyString(), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/hotels")
                .param("page","0")
                .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testAddHotel_Unauthenticated() throws Exception {

        mockMvc.perform(post("/api/hotels")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}