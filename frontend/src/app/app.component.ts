import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar no-print">
      <div class="container nav-inner">
        <a routerLink="/hotels" class="brand">
          <span class="brand-icon">🏨</span>
          <span class="brand-name">HotelBooking</span>
        </a>
        <ul class="nav-links">
          <li>
            <a routerLink="/hotels" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}">
              Explore Hotels
            </a>
          </li>
          <li>
            <a routerLink="/manage" routerLinkActive="active">
              Manage Booking
            </a>
          </li>
        </ul>
      </div>
    </nav>
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .navbar {
      background: #fff;
      border-bottom: 1px solid var(--border);
      position: sticky;
      top: 0;
      z-index: 100;
      box-shadow: var(--shadow);
    }
    .nav-inner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 64px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      text-decoration: none;
      color: var(--text-primary);
    }
    .brand-icon { font-size: 1.5rem; }
    .brand-name {
      font-size: 1.2rem;
      font-weight: 700;
      color: var(--primary);
    }
    .nav-links {
      list-style: none;
      display: flex;
      gap: 2rem;
      a {
        color: var(--text-secondary);
        font-weight: 500;
        font-size: 0.95rem;
        text-decoration: none;
        padding: 0.4rem 0;
        border-bottom: 2px solid transparent;
        transition: all 0.2s;
        &:hover, &.active {
          color: var(--primary);
          border-bottom-color: var(--primary);
          text-decoration: none;
        }
      }
    }
    main { padding-top: 0; }
    @media (max-width: 600px) {
      .brand-name { display: none; }
      .nav-links { gap: 1rem; }
    }
  `]
})
export class AppComponent {}
