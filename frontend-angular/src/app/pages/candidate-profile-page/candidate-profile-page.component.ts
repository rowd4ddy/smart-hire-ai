import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-candidate-profile-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './candidate-profile-page.component.html'
})
export class CandidateProfilePageComponent {
  private readonly authService = inject(AuthService);

  get fullName(): string {
    return this.authService.getFullName();
  }

  readonly placeholderSkills: string[] = ['Communication', 'Problem solving', 'Java', 'SQL'];
  readonly experienceYears = 2;
  readonly educationLevel = 'Bachelor';
}
