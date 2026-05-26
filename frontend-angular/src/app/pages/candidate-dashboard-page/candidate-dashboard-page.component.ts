import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CvService, CvVersion } from '../../services/cv.service';
import { Job, JobService } from '../../services/job.service';

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
  cvVersions = signal<CvVersion[]>([]);
  isLoadingCvs = signal(true);
  isUploadingCv = signal(false);
  selectedCvFile = signal<File | null>(null);
  cvError = signal('');

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

    this.loadCvVersions();
  }

  logout(): void {
    this.authService.logout();
  }

  get currentCv(): CvVersion | null {
    return this.cvVersions().find((version) => version.active) ?? this.cvVersions()[0] ?? null;
  }

  onCvSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.cvError.set('');
    this.selectedCvFile.set(file);
  }

  uploadSelectedCv(): void {
    const file = this.selectedCvFile();

    if (!file) {
      this.cvError.set('Choose a PDF, DOC, or DOCX file first.');
      return;
    }

    this.isUploadingCv.set(true);
    this.cvError.set('');

    this.cvService.uploadCv(file).subscribe({
      next: (cvVersion) => {
        this.cvVersions.update((versions) => [
          cvVersion,
          ...versions.map((version) => ({ ...version, active: false }))
        ]);
        this.selectedCvFile.set(null);
        this.isUploadingCv.set(false);
      },
      error: (err) => {
        this.cvError.set(err.error?.message ?? 'Failed to upload CV. Please try again.');
        this.isUploadingCv.set(false);
      }
    });
  }

  formatFileSize(size: number): string {
    if (size < 1024 * 1024) {
      return `${Math.max(1, Math.round(size / 1024))} KB`;
    }

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  private loadCvVersions(): void {
    this.cvService.getCvVersions().subscribe({
      next: (versions) => {
        this.cvVersions.set(versions);
        this.isLoadingCvs.set(false);
      },
      error: () => {
        this.cvError.set('Failed to load CV versions.');
        this.isLoadingCvs.set(false);
      }
    });
  }
}
