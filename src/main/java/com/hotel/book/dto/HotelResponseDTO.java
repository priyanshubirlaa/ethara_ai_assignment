package com.hotel.book.dto;

import java.io.Serializable;

public class HotelResponseDTO implements Serializable {

    private Long id;
    private String name;
    private String location;

    public HotelResponseDTO() {
    }

    public HotelResponseDTO(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public static HotelResponseDTOBuilder builder() {
        return new HotelResponseDTOBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static class HotelResponseDTOBuilder {
        private Long id;
        private String name;
        private String location;

        public HotelResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public HotelResponseDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public HotelResponseDTOBuilder location(String location) {
            this.location = location;
            return this;
        }

        public HotelResponseDTO build() {
            return new HotelResponseDTO(id, name, location);
        }
    }
}
