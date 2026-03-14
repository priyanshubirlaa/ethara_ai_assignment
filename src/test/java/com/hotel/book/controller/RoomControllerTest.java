package com.hotel.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.RoomRequestDTO;
import com.hotel.book.dto.RoomResponseDTO;
import com.hotel.book.exception.ResourceNotFoundException;
import com.hotel.book.security.JwtFilter;
import com.hotel.book.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(controllers = RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtFilter jwtFilter;

    private RoomRequestDTO roomRequestDTO;
    private RoomResponseDTO roomResponseDTO;

    @BeforeEach
    void setUp() {

        roomRequestDTO = new RoomRequestDTO();
        roomRequestDTO.setType("Deluxe");
        roomRequestDTO.setPrice(150.0);

        roomResponseDTO = RoomResponseDTO.builder()
                .id(1L)
                .type("Deluxe")
                .price(150.0)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddRoom_Success() throws Exception {

        when(roomService.addRoomToHotel(eq(1L), any()))
                .thenReturn(roomResponseDTO);

        mockMvc.perform(post("/api/hotels/1/rooms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("Deluxe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRooms_Success() throws Exception {

        Page<RoomResponseDTO> page =
                new PageImpl<>(List.of(roomResponseDTO), PageRequest.of(0,10),1);

        when(roomService.getRoomsByHotel(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/hotels/1/rooms/available")
                .param("page","0")
                .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testAddRoom_Unauthenticated() throws Exception {

        mockMvc.perform(post("/api/hotels/1/rooms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}