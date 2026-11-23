package com.example.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AlternateDateDto {
    private LocalDate suggestedCheckIn;
    private LocalDate suggestedCheckOut;
    private int availableRooms;
    private int dayShift;
}
