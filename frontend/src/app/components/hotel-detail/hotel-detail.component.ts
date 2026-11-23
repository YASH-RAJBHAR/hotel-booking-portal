import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HotelService } from '../../services/hotel.service';
import { BookingService } from '../../services/booking.service';
import { HotelDetail, AlternateDate } from '../../models/hotel.model';
import { BookingResponse } from '../../models/booking.model';

@Component({
  selector: 'app-hotel-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './hotel-detail.component.html',
  styleUrls: ['./hotel-detail.component.scss']
})
export class HotelDetailComponent implements OnInit {
  hotel: HotelDetail | null = null;
  booking: BookingResponse | null = null;
  alternateDates: AlternateDate[] = [];
  loadingHotel = false;
  loadingAlternate = false;
  bookingLoading = false;
  error = '';
  bookingError = '';
  checkIn = '';
  checkOut = '';
  today: string;
  bookingForm: FormGroup;
  showVoucher = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private hotelService: HotelService,
    private bookingService: BookingService
  ) {
    const now = new Date();
    this.today = now.toISOString().split('T')[0];
    this.bookingForm = this.fb.group({
      guestName: ['', [Validators.required, Validators.maxLength(100)]],
      guestEmail: ['', [Validators.required, Validators.email, Validators.maxLength(150)]]
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.route.queryParams.subscribe(params => {
      this.checkIn = params['checkIn'] || '';
      this.checkOut = params['checkOut'] || '';
      this.loadHotel(id);
    });
  }

  loadHotel(id: number): void {
    this.loadingHotel = true;
    this.error = '';
    this.hotelService.getHotel(id, this.checkIn, this.checkOut).subscribe({
      next: (data) => {
        this.hotel = data;
        this.loadingHotel = false;
        if (this.checkIn && this.checkOut && data.availableRooms === 0) {
          this.fetchAlternateDates(id);
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load hotel details.';
        this.loadingHotel = false;
      }
    });
  }

  fetchAlternateDates(id: number): void {
    if (!this.checkIn || !this.checkOut) return;
    this.loadingAlternate = true;
    this.hotelService.getAlternateDates(id, this.checkIn, this.checkOut).subscribe({
      next: (data) => { this.alternateDates = data; this.loadingAlternate = false; },
      error: () => { this.loadingAlternate = false; }
    });
  }

  selectAlternate(alt: AlternateDate): void {
    this.checkIn = alt.suggestedCheckIn;
    this.checkOut = alt.suggestedCheckOut;
    this.alternateDates = [];
    this.loadHotel(this.hotel!.id);
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { checkIn: this.checkIn, checkOut: this.checkOut },
      queryParamsHandling: 'merge'
    });
  }

  submitBooking(): void {
    if (this.bookingForm.invalid) { this.bookingForm.markAllAsTouched(); return; }
    if (!this.checkIn || !this.checkOut) { this.bookingError = 'Please select check-in and check-out dates.'; return; }
    this.bookingLoading = true;
    this.bookingError = '';
    const req = {
      hotelId: this.hotel!.id,
      guestName: this.bookingForm.value.guestName,
      guestEmail: this.bookingForm.value.guestEmail,
      checkInDate: this.checkIn,
      checkOutDate: this.checkOut
    };
    this.bookingService.createBooking(req).subscribe({
      next: (resp) => { this.booking = resp; this.showVoucher = true; this.bookingLoading = false; },
      error: (err) => { this.bookingError = err.error?.message || 'Booking failed. Please try again.'; this.bookingLoading = false; }
    });
  }

  printVoucher(): void { window.print(); }

  get minCheckOut(): string {
    if (!this.checkIn) return this.today;
    const d = new Date(this.checkIn);
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  }

  amenitiesList(amenities: string): string[] {
    return amenities ? amenities.split(',').map(a => a.trim()) : [];
  }

  formatPrice(p: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'long', year: 'numeric' });
  }

  get f() { return this.bookingForm.controls; }
}
