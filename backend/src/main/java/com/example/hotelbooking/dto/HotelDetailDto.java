package com.example.hotelbooking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HotelDetailDto {
    private Long id;
    private String name;
    private String location;
    private String description;
    private String amenities;
    private String roomCategory;
    private BigDecimal pricePerNight;
    private Integer totalRooms;
    private Integer availableRooms;
    private String imageUrl;
    // Price preview
    private Integer nights;
    private BigDecimal totalAmount;
}
