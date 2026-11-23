-- ============================================================
-- Hotel Booking Portal – Database Schema
-- Database: hotel_booking_db  |  MySQL 8+
-- ============================================================

CREATE DATABASE IF NOT EXISTS hotel_booking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_booking_db;

-- ============================================================
-- Table: hotels
-- ============================================================
CREATE TABLE IF NOT EXISTS hotels (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)    NOT NULL,
    location        VARCHAR(100)    NOT NULL,
    description     TEXT,
    amenities       VARCHAR(500),
    room_category   VARCHAR(100),
    price_per_night DECIMAL(10, 2)  NOT NULL,
    total_rooms     INT             NOT NULL,
    image_url       VARCHAR(500),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- Table: bookings
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    booking_reference VARCHAR(20)     NOT NULL,
    hotel_id          BIGINT          NOT NULL,
    guest_name        VARCHAR(100)    NOT NULL,
    guest_email       VARCHAR(150)    NOT NULL,
    check_in_date     DATE            NOT NULL,
    check_out_date    DATE            NOT NULL,
    nights            INT             NOT NULL,
    total_amount      DECIMAL(10, 2)  NOT NULL,
    status            ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_booking_reference (booking_reference),
    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- Composite index for fast overlap queries
    INDEX idx_booking_hotel_status_dates (hotel_id, status, check_in_date, check_out_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
