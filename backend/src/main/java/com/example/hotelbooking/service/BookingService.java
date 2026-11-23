package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequest;
import com.example.hotelbooking.dto.BookingResponse;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Booking.BookingStatus;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.exception.BookingConflictException;
import com.example.hotelbooking.exception.BookingNotFoundException;
import com.example.hotelbooking.exception.BookingValidationException;
import com.example.hotelbooking.exception.HotelNotFoundException;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // --- Date validation ---
        LocalDate today = LocalDate.now();
        if (request.getCheckInDate().isBefore(today)) {
            throw new BookingValidationException("Check-in date cannot be in the past.");
        }
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BookingValidationException("Check-out date must be after check-in date.");
        }

        // --- Pessimistic lock on hotel row to prevent concurrent over-booking ---
        Hotel hotel = hotelRepository.findByIdWithLock(request.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));

        // --- Availability check ---
        long overlapping = bookingRepository.countOverlappingBookings(
                hotel.getId(), BookingStatus.CONFIRMED,
                request.getCheckInDate(), request.getCheckOutDate());
        int available = hotel.getTotalRooms() - (int) overlapping;
        if (available <= 0) {
            throw new BookingConflictException(
                    "No rooms available at " + hotel.getName() + " for the selected dates.");
        }

        // --- Backend price calculation (never trust frontend totals) ---
        int nights = (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = hotel.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        // --- Generate unique booking reference ---
        String reference = generateReference();

        // --- Persist ---
        Booking booking = Booking.builder()
                .bookingReference(reference)
                .hotel(hotel)
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail().toLowerCase())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .nights(nights)
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);
        return toResponse(booking, hotel);
    }

    @Transactional(readOnly = true)
    public BookingResponse lookupBooking(String reference, String email) {
        Booking booking = bookingRepository
                .findByBookingReferenceAndGuestEmailIgnoreCase(reference, email)
                .orElseThrow(() -> new BookingNotFoundException(
                        "No booking found for reference '" + reference + "' and the provided email."));
        return toResponse(booking, booking.getHotel());
    }

    @Transactional
    public BookingResponse cancelBooking(String reference, String email) {
        Booking booking = bookingRepository
                .findByBookingReferenceAndGuestEmailIgnoreCase(reference, email)
                .orElseThrow(() -> new BookingNotFoundException(
                        "No booking found for reference '" + reference + "' and the provided email."));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingValidationException("Booking " + reference + " is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        return toResponse(booking, booking.getHotel());
    }

    private String generateReference() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        String candidate;
        do {
            StringBuilder sb = new StringBuilder("TC-");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            candidate = sb.toString();
        } while (bookingRepository.findByBookingReference(candidate).isPresent());
        return candidate;
    }

    private BookingResponse toResponse(Booking b, Hotel h) {
        return BookingResponse.builder()
                .id(b.getId())
                .bookingReference(b.getBookingReference())
                .hotelId(h.getId())
                .hotelName(h.getName())
                .hotelLocation(h.getLocation())
                .guestName(b.getGuestName())
                .guestEmail(b.getGuestEmail())
                .checkInDate(b.getCheckInDate())
                .checkOutDate(b.getCheckOutDate())
                .nights(b.getNights())
                .pricePerNight(h.getPricePerNight())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus().name())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
