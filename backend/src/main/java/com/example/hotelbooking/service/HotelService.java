package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.AlternateDateDto;
import com.example.hotelbooking.dto.HotelDetailDto;
import com.example.hotelbooking.dto.HotelDto;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.entity.Booking.BookingStatus;
import com.example.hotelbooking.exception.HotelNotFoundException;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<HotelDto> searchHotels(String location, LocalDate checkIn, LocalDate checkOut) {
        List<Hotel> hotels = (location != null && !location.isBlank())
                ? hotelRepository.findByLocationIgnoreCase(location.trim())
                : hotelRepository.findAll();

        return hotels.stream()
                .map(h -> toHotelDto(h, checkIn, checkOut))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelDetailDto getHotelDetail(Long id, LocalDate checkIn, LocalDate checkOut) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        int available = computeAvailable(hotel, checkIn, checkOut);
        Integer nights = null;
        BigDecimal total = null;
        if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
            total = hotel.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        }

        return HotelDetailDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .description(hotel.getDescription())
                .amenities(hotel.getAmenities())
                .roomCategory(hotel.getRoomCategory())
                .pricePerNight(hotel.getPricePerNight())
                .totalRooms(hotel.getTotalRooms())
                .availableRooms(available)
                .imageUrl(hotel.getImageUrl())
                .nights(nights)
                .totalAmount(total)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AlternateDateDto> getAlternateDates(Long id, LocalDate checkIn, LocalDate checkOut) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        long duration = ChronoUnit.DAYS.between(checkIn, checkOut);
        List<AlternateDateDto> suggestions = new ArrayList<>();

        for (int shift = 1; shift <= 7 && suggestions.size() < 3; shift++) {
            // Try shift forward
            LocalDate fwdIn = checkIn.plusDays(shift);
            LocalDate fwdOut = checkOut.plusDays(shift);
            long fwdOverlap = bookingRepository.countOverlappingConfirmed(id, fwdIn, fwdOut);
            int fwdAvail = hotel.getTotalRooms() - (int) fwdOverlap;
            if (fwdAvail > 0) {
                suggestions.add(new AlternateDateDto(fwdIn, fwdOut, fwdAvail, shift));
            }

            // Try shift backward (only if still in future)
            LocalDate bwdIn = checkIn.minusDays(shift);
            LocalDate bwdOut = checkOut.minusDays(shift);
            if (!bwdIn.isBefore(LocalDate.now()) && suggestions.size() < 3) {
                long bwdOverlap = bookingRepository.countOverlappingConfirmed(id, bwdIn, bwdOut);
                int bwdAvail = hotel.getTotalRooms() - (int) bwdOverlap;
                if (bwdAvail > 0) {
                    suggestions.add(new AlternateDateDto(bwdIn, bwdOut, bwdAvail, -shift));
                }
            }
        }
        return suggestions;
    }

    private HotelDto toHotelDto(Hotel h, LocalDate checkIn, LocalDate checkOut) {
        return HotelDto.builder()
                .id(h.getId())
                .name(h.getName())
                .location(h.getLocation())
                .description(h.getDescription())
                .amenities(h.getAmenities())
                .roomCategory(h.getRoomCategory())
                .pricePerNight(h.getPricePerNight())
                .totalRooms(h.getTotalRooms())
                .availableRooms(computeAvailable(h, checkIn, checkOut))
                .imageUrl(h.getImageUrl())
                .build();
    }

    int computeAvailable(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return hotel.getTotalRooms();
        }
        long overlapping = bookingRepository.countOverlappingBookings(
                hotel.getId(), BookingStatus.CONFIRMED, checkIn, checkOut);
        return Math.max(0, hotel.getTotalRooms() - (int) overlapping);
    }
}
