package com.hotel.book.entity;


public enum RoomType {
    STANDARD_ROOM("Standard Room"),
    SUPERIOR_ROOM("Superior Room"),
    DELUXE_ROOM("Deluxe Room"),
    EXECUTIVE_SUITE("Executive Suite");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RoomType fromDisplayName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        for (RoomType type : values()) {
            if (type.displayName.toLowerCase().equals(normalized)) {
                return type;
            }
        }
        // also allow enum name style inputs like STANDARD_ROOM
        try {
            return RoomType.valueOf(value.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

