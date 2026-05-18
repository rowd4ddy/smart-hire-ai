import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-account-settings-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './account-settings-page.component.html'
})
export class AccountSettingsPageComponent {
  private readonly authService = inject(AuthService);

  fullName = this.authService.getFullName();
  successMessage = signal('');

  get role(): string {
    return this.authService.getRole() ?? '';
  }

  get dashboardLink(): string {
    return this.authService.getDashboardLink();
  }

  logout(): void {
    this.authService.logout();
  }
}
