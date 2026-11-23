package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Optional<Booking> findByBookingReferenceAndGuestEmailIgnoreCase(
        String bookingReference, String guestEmail);

    /**
     * Counts CONFIRMED bookings that overlap [checkIn, checkOut) for the given hotel.
     * Overlap condition: existing.checkInDate < requestedCheckOut AND existing.checkOutDate > requestedCheckIn
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.hotel.id = :hotelId
          AND b.status = :status
          AND b.checkInDate < :checkOut
          AND b.checkOutDate > :checkIn
    """)
    long countOverlappingBookings(
        @Param("hotelId") Long hotelId,
        @Param("status") BookingStatus status,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    /**
     * Finds dates within a window where availability > 0.
     * Used by the alternate-dates suggestion feature.
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.hotel.id = :hotelId
          AND b.status = 'CONFIRMED'
          AND b.checkInDate < :checkOut
          AND b.checkOutDate > :checkIn
    """)
    long countOverlappingConfirmed(
        @Param("hotelId") Long hotelId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}
