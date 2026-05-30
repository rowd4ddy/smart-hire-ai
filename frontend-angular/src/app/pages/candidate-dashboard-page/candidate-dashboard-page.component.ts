import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Job, JobService } from '../../services/job.service';
import { CandidateProfile, CvService } from '../../services/cv.service';

@Component({
  selector: 'app-candidate-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './candidate-dashboard-page.component.html'
})
export class CandidateDashboardPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly jobService = inject(JobService);
  private readonly cvService = inject(CvService);

  jobs = signal<Job[]>([]);
  isLoadingJobs = signal(true);
  jobsError = signal('');
  profile = signal<CandidateProfile | null>(null);

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
    this.jobService.getJobs().subscribe({
      next: (data) => {
        this.jobs.set(data.slice(0, 3));
        this.isLoadingJobs.set(false);
      },
      error: () => {
        this.jobsError.set('Failed to load recommended jobs.');
        this.isLoadingJobs.set(false);
      }
    });

    this.cvService.getCandidateProfile().subscribe({
      next: (data) => this.profile.set(data),
      error: () => {
        // Candidate profile may not exist yet
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
