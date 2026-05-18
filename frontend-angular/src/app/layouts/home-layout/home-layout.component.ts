import { Component, HostListener, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home-layout',
  standalone: true,
  imports: [RouterLink, RouterOutlet],
  templateUrl: './home-layout.component.html'
})
export class HomeLayoutComponent {
  private readonly authService = inject(AuthService);

  mobileMenuOpen = false;
  activeNav = 'home';

  readonly navItems = [
    { href: '#features', label: 'Features' },
    { href: '#workflow', label: 'Workflow' },
    { href: '#impact', label: 'Impact' },
    { href: '/jobs', routerLink: '/jobs', label: 'Jobs' }
  ];

  readonly bottomNavItems = [
    { id: 'home', href: '#', label: 'Home' },
    { id: 'features', href: '#features', label: 'Features' },
    { id: 'workflow', href: '#workflow', label: 'Workflow' },
    { id: 'jobs', href: '/jobs', routerLink: '/jobs', label: 'Jobs' },
    { id: 'impact', href: '#impact', label: 'Impact' }
  ];

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  dashboardLink(): string {
    return this.authService.getDashboardLink();
  }

  logout(): void {
    this.closeMobileMenu();
    this.authService.logout();
  }

  setActiveNav(id: string): void {
    this.activeNav = id;
    this.closeMobileMenu();
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    const sections = ['impact', 'workflow', 'features'];
    const scrollY = window.scrollY + 120;
    let current = 'home';

    for (const id of sections) {
      const section = document.getElementById(id);

      if (section && scrollY >= section.offsetTop) {
        current = id;
        break;
      }
    }

    this.activeNav = current;
  }
}
