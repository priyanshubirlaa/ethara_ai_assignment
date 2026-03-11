package com.hotel.book.dto;

import java.io.Serializable;

public class RoomResponseDTO implements Serializable{

    private Long id;
    private String type;
    private Double price;
    //private Boolean available;

    public RoomResponseDTO() {
    }

    public RoomResponseDTO(Long id, String type, Double price, Boolean available) {
        this.id = id;
        this.type = type;
        this.price = price;
       // this.available = available;
    }

    public static RoomResponseDTOBuilder builder() {
        return new RoomResponseDTOBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // public Boolean getAvailable() {
    //     return available;
    // }

    // public void setAvailable(Boolean available) {
    //     this.available = available;
    // }

    public static class RoomResponseDTOBuilder {
        private Long id;
        private String type;
        private Double price;
        private Boolean available;

        public RoomResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoomResponseDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public RoomResponseDTOBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public RoomResponseDTOBuilder available(Boolean available) {
            this.available = available;
            return this;
        }

        public RoomResponseDTO build() {
            return new RoomResponseDTO(id, type, price, available);
        }
    }
}
