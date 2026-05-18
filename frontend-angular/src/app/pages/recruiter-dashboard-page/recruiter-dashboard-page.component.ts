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
  selector: 'app-recruiter-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './recruiter-dashboard-page.component.html'
})
export class RecruiterDashboardPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);

  jobs = signal<Job[]>([]);
  isLoadingJobs = signal(true);
  jobsError = signal('');

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
    this.http.get<Job[]>('http://localhost:8080/api/jobs').subscribe({
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

  logout(): void {
    this.authService.logout();
  }

  getScoreColor(score: number): string {
    if (score >= 90) return '#22C55E';
    if (score >= 80) return '#4F46E5';
    return '#8B5CF6';
  }
}
