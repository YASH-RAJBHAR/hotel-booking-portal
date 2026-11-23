import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'hotels',
    pathMatch: 'full'
  },
  {
    path: 'hotels',
    loadComponent: () =>
      import('./components/hotel-list/hotel-list.component').then(m => m.HotelListComponent)
  },
  {
    path: 'hotels/:id',
    loadComponent: () =>
      import('./components/hotel-detail/hotel-detail.component').then(m => m.HotelDetailComponent)
  },
  {
    path: 'manage',
    loadComponent: () =>
      import('./components/manage-booking/manage-booking.component').then(m => m.ManageBookingComponent)
  },
  {
    path: '**',
    redirectTo: 'hotels'
  }
];
