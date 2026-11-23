import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HotelService } from '../../services/hotel.service';
import { Hotel } from '../../models/hotel.model';

@Component({
  selector: 'app-hotel-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './hotel-list.component.html',
  styleUrls: ['./hotel-list.component.scss']
})
export class HotelListComponent implements OnInit {
  hotels: Hotel[] = [];
  loading = false;
  error = '';
  searchForm: FormGroup;
  today: string;
  locations = ['', 'Mumbai', 'Goa', 'Jaipur', 'Udaipur', 'Bengaluru', 'Manali'];

  constructor(private fb: FormBuilder, private hotelService: HotelService) {
    const now = new Date();
    this.today = now.toISOString().split('T')[0];
    this.searchForm = this.fb.group({
      location: [''],
      checkIn: [''],
      checkOut: ['']
    });
  }

  ngOnInit(): void {
    this.loadHotels();
  }

  loadHotels(): void {
    this.loading = true;
    this.error = '';
    const { location, checkIn, checkOut } = this.searchForm.value;
    this.hotelService.searchHotels(location, checkIn, checkOut).subscribe({
      next: (data) => { this.hotels = data; this.loading = false; },
      error: (err) => { this.error = err.error?.message || 'Failed to load hotels.'; this.loading = false; }
    });
  }

  get minCheckOut(): string {
    const ci = this.searchForm.get('checkIn')?.value;
    if (!ci) {
      const d = new Date();
      d.setDate(d.getDate() + 1);
      return d.toISOString().split('T')[0];
    }
    const d = new Date(ci);
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  }

  get searchParams() {
    return this.searchForm.value;
  }

  amenitiesList(amenities: string): string[] {
    return amenities ? amenities.split(',').map(a => a.trim()) : [];
  }

  formatPrice(p: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);
  }
}
