package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequest;
import com.example.hotelbooking.dto.BookingResponse;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Booking.BookingStatus;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.exception.BookingConflictException;
import com.example.hotelbooking.exception.BookingValidationException;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private BookingService bookingService;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .location("Mumbai")
                .pricePerNight(new BigDecimal("5000.00"))
                .totalRooms(3)
                .build();
    }

    @Test
    @DisplayName("Valid booking should compute total amount correctly")
    void createBooking_validRequest_computesTotalCorrectly() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(3); // 3 nights

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setGuestName("Alice");
        request.setGuestEmail("alice@example.com");
        request.setCheckInDate(checkIn);
        request.setCheckOutDate(checkOut);

        when(hotelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(hotel));
        when(bookingRepository.countOverlappingBookings(eq(1L), eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(0L);
        when(bookingRepository.findByBookingReference(anyString())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response.getNights()).isEqualTo(3);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getBookingReference()).startsWith("TC-");
    }

    @Test
    @DisplayName("Check-in date in the past should throw BookingValidationException")
    void createBooking_pastCheckIn_throwsValidationException() {
        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setGuestName("Bob");
        request.setGuestEmail("bob@example.com");
        request.setCheckInDate(LocalDate.now().minusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Check-in date cannot be in the past");
    }

    @Test
    @DisplayName("Check-out before check-in should throw BookingValidationException")
    void createBooking_checkOutBeforeCheckIn_throwsValidationException() {
        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setGuestName("Carol");
        request.setGuestEmail("carol@example.com");
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    @DisplayName("Same check-in and check-out date should throw BookingValidationException")
    void createBooking_sameCheckInAndCheckOut_throwsValidationException() {
        LocalDate date = LocalDate.now().plusDays(2);
        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setGuestName("Dave");
        request.setGuestEmail("dave@example.com");
        request.setCheckInDate(date);
        request.setCheckOutDate(date);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BookingValidationException.class);
    }

    @Test
    @DisplayName("Sold-out hotel should throw BookingConflictException")
    void createBooking_soldOut_throwsConflictException() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setGuestName("Eve");
        request.setGuestEmail("eve@example.com");
        request.setCheckInDate(checkIn);
        request.setCheckOutDate(checkOut);

        when(hotelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(hotel));
        // All 3 rooms are booked
        when(bookingRepository.countOverlappingBookings(eq(1L), eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(3L);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("No rooms available");
    }

    @Test
    @DisplayName("Same-day checkout and next-day check-in should NOT be an overlap")
    void availability_sameDay_checkoutAndCheckin_noOverlap() {
        // Existing booking: Jan 10 -> Jan 15
        // New booking: Jan 15 -> Jan 18 (checkout on same day as existing checkin is NOT overlap)
        LocalDate existingCheckOut = LocalDate.now().plusDays(10);
        LocalDate newCheckIn = existingCheckOut; // same day
        LocalDate newCheckOut = existingCheckOut.plusDays(3);

        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setGuestName("Frank");
        request.setGuestEmail("frank@example.com");
        request.setCheckInDate(newCheckIn);
        request.setCheckOutDate(newCheckOut);

        when(hotelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(hotel));
        // 0 overlapping means the same-day boundary is not counted
        when(bookingRepository.countOverlappingBookings(eq(1L), eq(BookingStatus.CONFIRMED),
                eq(newCheckIn), eq(newCheckOut))).thenReturn(0L);
        when(bookingRepository.findByBookingReference(anyString())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(200L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request);
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Cancelling a booking should set status to CANCELLED")
    void cancelBooking_confirmedBooking_returnsCancelled() {
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference("TC-TEST1")
                .hotel(hotel)
                .guestName("Grace")
                .guestEmail("grace@example.com")
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(5))
                .nights(3)
                .totalAmount(new BigDecimal("15000.00"))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findByBookingReferenceAndGuestEmailIgnoreCase("TC-TEST1", "grace@example.com"))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancelBooking("TC-TEST1", "grace@example.com");

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("Cancelling an already-cancelled booking should throw BookingValidationException")
    void cancelBooking_alreadyCancelled_throwsValidationException() {
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference("TC-TEST2")
                .hotel(hotel)
                .guestName("Henry")
                .guestEmail("henry@example.com")
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(5))
                .nights(3)
                .totalAmount(new BigDecimal("15000.00"))
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findByBookingReferenceAndGuestEmailIgnoreCase("TC-TEST2", "henry@example.com"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("TC-TEST2", "henry@example.com"))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("already cancelled");
    }
}
