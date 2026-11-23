import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { BookingResponse } from '../../models/booking.model';

@Component({
  selector: 'app-manage-booking',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './manage-booking.component.html',
  styleUrls: ['./manage-booking.component.scss']
})
export class ManageBookingComponent {
  lookupForm: FormGroup;
  booking: BookingResponse | null = null;
  loading = false;
  cancelling = false;
  error = '';
  cancelError = '';
  cancelSuccess = '';
  showCancelConfirm = false;

  constructor(private fb: FormBuilder, private bookingService: BookingService) {
    this.lookupForm = this.fb.group({
      reference: ['', [Validators.required, Validators.pattern(/^TC-[A-Z0-9]{6,8}$/)]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  lookup(): void {
    if (this.lookupForm.invalid) { this.lookupForm.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
    this.booking = null;
    this.cancelSuccess = '';
    const { reference, email } = this.lookupForm.value;
    this.bookingService.lookupBooking(reference, email).subscribe({
      next: (data) => { this.booking = data; this.loading = false; },
      error: (err) => { this.error = err.error?.message || 'Booking not found.'; this.loading = false; }
    });
  }

  confirmCancel(): void { this.showCancelConfirm = true; }
  dismissCancel(): void { this.showCancelConfirm = false; }

  cancelBooking(): void {
    if (!this.booking) return;
    this.cancelling = true;
    this.cancelError = '';
    this.showCancelConfirm = false;
    this.bookingService.cancelBooking(this.booking.bookingReference, this.booking.guestEmail).subscribe({
      next: (data) => {
        this.booking = data;
        this.cancelSuccess = 'Your booking has been successfully cancelled.';
        this.cancelling = false;
      },
      error: (err) => {
        this.cancelError = err.error?.message || 'Cancellation failed. Please try again.';
        this.cancelling = false;
      }
    });
  }

  printVoucher(): void { window.print(); }

  formatPrice(p: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'long', year: 'numeric' });
  }

  get f() { return this.lookupForm.controls; }
}
