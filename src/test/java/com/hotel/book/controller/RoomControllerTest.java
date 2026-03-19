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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoomController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
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
    void testAddRoomToHotel_Success() throws Exception {
        when(roomService.addRoomToHotel(eq(1L), any(RoomRequestDTO.class))).thenReturn(roomResponseDTO);

        mockMvc.perform(post("/api/hotels/1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("Deluxe"))
                .andExpect(jsonPath("$.price").value(150.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddRoomToHotel_ValidationError() throws Exception {
        roomRequestDTO.setType(""); // Invalid: empty type

        mockMvc.perform(post("/api/hotels/1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddRoomToHotel_HotelNotFound() throws Exception {
        when(roomService.addRoomToHotel(eq(999L), any(RoomRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Hotel not found"));

        mockMvc.perform(post("/api/hotels/999/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hotel not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAvailableRooms_Success() throws Exception {
        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO);
        Page<RoomResponseDTO> page = new PageImpl<>(rooms, PageRequest.of(0, 10), 1L);

        when(roomService.getRoomsByHotel(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/hotels/1/rooms/available")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].type").value("Deluxe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRoom_Success() throws Exception {
        when(roomService.getRoomByHotelAndRoom(1L, 1L)).thenReturn(roomResponseDTO);

        mockMvc.perform(get("/api/hotels/1/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("Deluxe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRoom_NotFound() throws Exception {
        when(roomService.getRoomByHotelAndRoom(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Room not found for this hotel"));

        mockMvc.perform(get("/api/hotels/1/rooms/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Room not found for this hotel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchRooms_Success() throws Exception {
        List<RoomResponseDTO> rooms = Arrays.asList(roomResponseDTO);
        Page<RoomResponseDTO> page = new PageImpl<>(rooms, PageRequest.of(0, 10), 1L);

        when(roomService.searchRooms(anyLong(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/hotels/1/rooms")
                        .param("minPrice", "100")
                        .param("maxPrice", "200")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void testAddRoomToHotel_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/hotels/1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddRoomToHotel_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/hotels/1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}
