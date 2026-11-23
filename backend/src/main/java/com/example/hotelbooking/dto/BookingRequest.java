package com.example.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Guest name is required")
    @Size(max = 100, message = "Guest name must be at most 100 characters")
    private String guestName;

    @NotBlank(message = "Guest email is required")
    @Email(message = "Guest email must be a valid email address")
    @Size(max = 150, message = "Guest email must be at most 150 characters")
    private String guestEmail;

    @NotNull(message = "Check-in date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
}
