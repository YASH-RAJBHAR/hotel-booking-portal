export interface Hotel {
  id: number;
  name: string;
  location: string;
  description: string;
  amenities: string;
  roomCategory: string;
  pricePerNight: number;
  totalRooms: number;
  availableRooms: number;
  imageUrl: string;
}

export interface HotelDetail extends Hotel {
  nights?: number;
  totalAmount?: number;
}

export interface AlternateDate {
  suggestedCheckIn: string;
  suggestedCheckOut: string;
  availableRooms: number;
  dayShift: number;
}
