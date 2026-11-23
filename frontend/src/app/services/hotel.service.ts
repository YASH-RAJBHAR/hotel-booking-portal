import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Hotel, HotelDetail, AlternateDate } from '../models/hotel.model';

@Injectable({ providedIn: 'root' })
export class HotelService {
  private readonly base = '/api/hotels';

  constructor(private http: HttpClient) {}

  searchHotels(location?: string, checkIn?: string, checkOut?: string): Observable<Hotel[]> {
    let params = new HttpParams();
    if (location) params = params.set('location', location);
    if (checkIn) params = params.set('checkIn', checkIn);
    if (checkOut) params = params.set('checkOut', checkOut);
    return this.http.get<Hotel[]>(this.base, { params });
  }

  getHotel(id: number, checkIn?: string, checkOut?: string): Observable<HotelDetail> {
    let params = new HttpParams();
    if (checkIn) params = params.set('checkIn', checkIn);
    if (checkOut) params = params.set('checkOut', checkOut);
    return this.http.get<HotelDetail>(`${this.base}/${id}`, { params });
  }

  getAlternateDates(id: number, checkIn: string, checkOut: string): Observable<AlternateDate[]> {
    const params = new HttpParams().set('checkIn', checkIn).set('checkOut', checkOut);
    return this.http.get<AlternateDate[]>(`${this.base}/${id}/alternate-dates`, { params });
  }
}
