-- ============================================================
-- Hotel Booking Portal – Seed Data
-- Run AFTER schema.sql
-- ============================================================
USE hotel_booking_db;

-- ============================================================
-- Hotels (6 realistic Indian properties)
-- ============================================================
INSERT INTO hotels (name, location, description, amenities, room_category, price_per_night, total_rooms, image_url)
VALUES
(
    'The Taj Mahal Palace',
    'Mumbai',
    'An iconic heritage hotel overlooking the Gateway of India and the Arabian Sea. Built in 1903, it blends Moorish, Oriental, and Florentine styles with legendary hospitality. Guests enjoy impeccably appointed rooms, world-class dining, and a serene spa.',
    'Free Wi-Fi,Swimming Pool,Spa & Wellness,24-Hour Room Service,Airport Shuttle,Concierge,Fine Dining,Fitness Centre',
    'Deluxe Sea-View King',
    12500.00,
    4,
    'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80'
),
(
    'Grand Hyatt Goa',
    'Goa',
    'A luxury resort spread over 28 acres of lush gardens beside the Bambolim Bay. The hotel features Goan-Portuguese architecture, five swimming pools, and direct beach access — perfect for a relaxing getaway on India''s sun-kissed coastline.',
    'Free Wi-Fi,5 Swimming Pools,Private Beach,Spa,Multiple Restaurants,Kids Club,Water Sports,Airport Shuttle',
    'Garden-View Deluxe Room',
    9800.00,
    5,
    'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=80'
),
(
    'Rambagh Palace',
    'Jaipur',
    'Once the residence of the Maharaja of Jaipur, this palatial hotel set in 47 acres of manicured Mughal gardens exudes royal grandeur. Ornate arches, hand-painted frescoes, and world-class cuisine make it a truly regal experience.',
    'Free Wi-Fi,Royal Swimming Pool,Spa,Horse Riding,Polo Grounds,Multiple Restaurants,Cultural Evenings,Fitness Centre',
    'Palace Room',
    18000.00,
    3,
    'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&q=80'
),
(
    'Taj Lake Palace',
    'Udaipur',
    'Rising like a white marble dream from the still waters of Lake Pichola, the Taj Lake Palace is accessible only by boat. Its ornate interiors, lotus pond, and rooftop terraces with Aravalli views make every moment magical.',
    'Free Wi-Fi,Outdoor Pool,Spa,Lake-View Dining,Boat Transfers,Yoga Classes,Sunset Cruises,Butlers',
    'Luxury Lake-View Room',
    22000.00,
    3,
    'https://images.unsplash.com/photo-1524492412937-b28074a5d7da?w=800&q=80'
),
(
    'The Leela Palace Bengaluru',
    'Bengaluru',
    'A sanctuary of refined luxury in the heart of India''s tech capital. With 357 rooms and suites, award-winning restaurants, and a world-class spa, The Leela Palace is the preferred address for business and leisure travellers in Bengaluru.',
    'Free Wi-Fi,Outdoor Pool,Spa,Multiple Restaurants,Business Centre,Fitness Centre,Airport Shuttle,Concierge',
    'Superior Room',
    8500.00,
    5,
    'https://images.unsplash.com/photo-1455587734955-081b22074882?w=800&q=80'
),
(
    'The Himalayan Hotel',
    'Manali',
    'Nestled at 2,050 m in the Kullu Valley, The Himalayan Hotel blends colonial charm with Himalayan warmth. Surrounded by apple orchards and deodar forests, it offers breathtaking mountain views, cosy fireplaces, and adventure activities at your doorstep.',
    'Free Wi-Fi,Mountain-View Restaurant,Bonfire Evenings,Trekking Guides,Airport Pickup,Room Heaters,Breakfast Included',
    'Mountain-View Deluxe Room',
    5500.00,
    4,
    'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?w=800&q=80'
);

-- ============================================================
-- Sample Bookings (for demonstration & date-overlap testing)
-- hotel_id 1 (Mumbai) has 4 rooms; create 2 confirmed bookings
-- hotel_id 3 (Jaipur) has 3 rooms; create 2 confirmed bookings (nearly sold out)
-- ============================================================

-- Booking 1 – Mumbai hotel, 3-night stay starting 5 days from today
INSERT INTO bookings (booking_reference, hotel_id, guest_name, guest_email, check_in_date, check_out_date, nights, total_amount, status)
VALUES (
    'TC-SAMPLE1',
    1,
    'Priya Sharma',
    'priya.sharma@example.com',
    DATE_ADD(CURDATE(), INTERVAL 5 DAY),
    DATE_ADD(CURDATE(), INTERVAL 8 DAY),
    3,
    37500.00,
    'CONFIRMED'
);

-- Booking 2 – Mumbai hotel, another guest same period
INSERT INTO bookings (booking_reference, hotel_id, guest_name, guest_email, check_in_date, check_out_date, nights, total_amount, status)
VALUES (
    'TC-SAMPLE2',
    1,
    'Rahul Verma',
    'rahul.verma@example.com',
    DATE_ADD(CURDATE(), INTERVAL 5 DAY),
    DATE_ADD(CURDATE(), INTERVAL 8 DAY),
    3,
    37500.00,
    'CONFIRMED'
);

-- Booking 3 – Jaipur palace, 2-night stay starting 7 days from today (1 of 3 rooms)
INSERT INTO bookings (booking_reference, hotel_id, guest_name, guest_email, check_in_date, check_out_date, nights, total_amount, status)
VALUES (
    'TC-SAMPLE3',
    3,
    'Anjali Singh',
    'anjali.singh@example.com',
    DATE_ADD(CURDATE(), INTERVAL 7 DAY),
    DATE_ADD(CURDATE(), INTERVAL 9 DAY),
    2,
    36000.00,
    'CONFIRMED'
);

-- Booking 4 – Jaipur palace, another guest same period (2 of 3 rooms)
INSERT INTO bookings (booking_reference, hotel_id, guest_name, guest_email, check_in_date, check_out_date, nights, total_amount, status)
VALUES (
    'TC-SAMPLE4',
    3,
    'Vikram Patel',
    'vikram.patel@example.com',
    DATE_ADD(CURDATE(), INTERVAL 7 DAY),
    DATE_ADD(CURDATE(), INTERVAL 9 DAY),
    2,
    36000.00,
    'CONFIRMED'
);

-- Booking 5 – Jaipur palace, 3rd room booked (sold out for those dates)
INSERT INTO bookings (booking_reference, hotel_id, guest_name, guest_email, check_in_date, check_out_date, nights, total_amount, status)
VALUES (
    'TC-SAMPLE5',
    3,
    'Meera Nair',
    'meera.nair@example.com',
    DATE_ADD(CURDATE(), INTERVAL 7 DAY),
    DATE_ADD(CURDATE(), INTERVAL 9 DAY),
    2,
    36000.00,
    'CONFIRMED'
);
