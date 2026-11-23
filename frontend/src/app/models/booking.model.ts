export interface BookingRequest {
  hotelId: number;
  guestName: string;
  guestEmail: string;
  checkInDate: string;
  checkOutDate: string;
}

export interface BookingResponse {
  id: number;
  bookingReference: string;
  hotelId: number;
  hotelName: string;
  hotelLocation: string;
  guestName: string;
  guestEmail: string;
  checkInDate: string;
  checkOutDate: string;
  nights: number;
  pricePerNight: number;
  totalAmount: number;
  status: 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}
