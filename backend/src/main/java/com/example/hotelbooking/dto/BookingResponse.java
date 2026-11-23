package com.example.hotelbooking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long hotelId;
    private String hotelName;
    private String hotelLocation;
    private String guestName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private BigDecimal pricePerNight;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
