package com.hotel.book.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;      // e.g. BOOKING_CREATED, BOOKING_CANCELLED

    private String entityType;  // e.g. BOOKING, ROOM, HOTEL

    private Long entityId;

    @Column(length = 1000)
    private String details;

    private String userEmail;

    private Instant timestamp;
}

