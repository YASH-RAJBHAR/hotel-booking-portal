package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByLocationIgnoreCase(String location);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Hotel h WHERE h.id = :id")
    Optional<Hotel> findByIdWithLock(@Param("id") Long id);
}
