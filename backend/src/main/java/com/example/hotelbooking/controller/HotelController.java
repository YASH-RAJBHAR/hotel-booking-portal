package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.AlternateDateDto;
import com.example.hotelbooking.dto.HotelDetailDto;
import com.example.hotelbooking.dto.HotelDto;
import com.example.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    /** GET /api/hotels?location=&checkIn=&checkOut= */
    @GetMapping
    public ResponseEntity<List<HotelDto>> searchHotels(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(hotelService.searchHotels(location, checkIn, checkOut));
    }

    /** GET /api/hotels/{id}?checkIn=&checkOut= */
    @GetMapping("/{id}")
    public ResponseEntity<HotelDetailDto> getHotel(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(hotelService.getHotelDetail(id, checkIn, checkOut));
    }

    /** GET /api/hotels/{id}/alternate-dates?checkIn=&checkOut= */
    @GetMapping("/{id}/alternate-dates")
    public ResponseEntity<List<AlternateDateDto>> alternateDates(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(hotelService.getAlternateDates(id, checkIn, checkOut));
    }
}
