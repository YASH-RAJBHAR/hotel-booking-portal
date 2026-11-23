package com.example.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String location;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String amenities;

    @Column(name = "room_category", length = 100)
    private String roomCategory;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "total_rooms", nullable = false)
    private Integer totalRooms;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
