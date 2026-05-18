import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

interface Job {
  id: number;
  title: string;
  company: string;
  requiredSkills: string[];
  minimumExperienceYears: number;
  educationLevel: string;
}

@Component({
  selector: 'app-candidate-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './candidate-dashboard-page.component.html'
})
export class CandidateDashboardPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);

  jobs = signal<Job[]>([]);
  isLoadingJobs = signal(true);
  jobsError = signal('');

  readonly recommendations = ['Improve Docker fundamentals', 'Complete Kubernetes basics', 'Review SQL optimization'];

  get fullName(): string {
    return this.authService.getFullName();
  }

  getInitials(): string {
    const name = this.fullName?.trim();
    if (!name) {
      return '?';
    }

    return name
      .split(' ')
      .filter((part) => part.length > 0)
      .map((part) => part[0])
      .join('');
  }

  ngOnInit(): void {
    this.http.get<Job[]>('http://localhost:8080/api/jobs').subscribe({
      next: (data) => {
        this.jobs.set(data.slice(0, 3));
        this.isLoadingJobs.set(false);
      },
      error: () => {
        this.jobsError.set('Failed to load recommended jobs.');
        this.isLoadingJobs.set(false);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
