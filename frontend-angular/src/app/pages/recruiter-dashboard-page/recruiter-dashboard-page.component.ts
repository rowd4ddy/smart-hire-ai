import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Job, JobService, RankedCandidate } from '../../services/job.service';

@Component({
  selector: 'app-recruiter-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './recruiter-dashboard-page.component.html'
})
export class RecruiterDashboardPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly jobService = inject(JobService);

  jobs = signal<Job[]>([]);
  isLoadingJobs = signal(true);
  jobsError = signal('');
  rankedCandidates = signal<RankedCandidate[]>([]);
  isLoadingCandidates = signal(false);
  selectedJobId = signal<number | null>(null);

  readonly mockCandidates = [
    { name: 'Nadia Amrani', initials: 'NA', score: 92, skills: 'Java, Spring Boot' },
    { name: 'Youssef Benali', initials: 'YB', score: 88, skills: 'PostgreSQL, React' },
    { name: 'Meriem Zahra', initials: 'MZ', score: 84, skills: 'Node.js, Docker' },
    { name: 'Karim Idrissi', initials: 'KI', score: 79, skills: 'Angular, TypeScript' }
  ];

  get fullName(): string {
    return this.authService.getFullName();
  }

  ngOnInit(): void {
    this.jobService.getJobs().subscribe({
      next: (data) => {
        this.jobs.set(data);
        this.isLoadingJobs.set(false);
      },
      error: () => {
        this.jobsError.set('Failed to load jobs.');
        this.isLoadingJobs.set(false);
      }
    });
  }

  selectJob(jobId: number): void {
    this.selectedJobId.set(jobId);
    this.isLoadingCandidates.set(true);
    this.jobService.getRankedCandidates(jobId).subscribe({
      next: (data) => {
        this.rankedCandidates.set(data);
        this.isLoadingCandidates.set(false);
      },
      error: () => {
        this.isLoadingCandidates.set(false);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }

  getCandidateInitials(fullName: string): string {
    if (!fullName) {
      return '?';
    }
    return fullName
      .split(' ')
      .filter((part) => part.length > 0)
      .map((part) => part[0])
      .join('');
  }

  getScoreColor(score: number): string {
    if (score >= 90) return '#22C55E';
    if (score >= 80) return '#4F46E5';
    return '#8B5CF6';
  }
}
