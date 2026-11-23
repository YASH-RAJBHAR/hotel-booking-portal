package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.BookingRequest;
import com.example.hotelbooking.dto.BookingResponse;
import com.example.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** POST /api/bookings */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    /** GET /api/bookings/lookup?reference=&email= */
    @GetMapping("/lookup")
    public ResponseEntity<BookingResponse> lookup(
            @RequestParam String reference,
            @RequestParam String email) {
        return ResponseEntity.ok(bookingService.lookupBooking(reference, email));
    }

    /** PATCH /api/bookings/{reference}/cancel?email= */
    @PatchMapping("/{reference}/cancel")
    public ResponseEntity<BookingResponse> cancel(
            @PathVariable String reference,
            @RequestParam String email) {
        return ResponseEntity.ok(bookingService.cancelBooking(reference, email));
    }
}
