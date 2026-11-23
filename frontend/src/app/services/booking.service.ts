import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookingRequest, BookingResponse } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly base = '/api/bookings';

  constructor(private http: HttpClient) {}

  createBooking(request: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(this.base, request);
  }

  lookupBooking(reference: string, email: string): Observable<BookingResponse> {
    const params = new HttpParams().set('reference', reference).set('email', email);
    return this.http.get<BookingResponse>(`${this.base}/lookup`, { params });
  }

  cancelBooking(reference: string, email: string): Observable<BookingResponse> {
    const params = new HttpParams().set('email', email);
    return this.http.patch<BookingResponse>(`${this.base}/${reference}/cancel`, {}, { params });
  }
}
