import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface Job {
  id: number;
  title: string;
  company: string;
  requiredSkills: string[];
  minimumExperienceYears: number;
  educationLevel: string;
}

@Component({
  selector: 'app-jobs-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './jobs-page.component.html'
})
export class JobsPageComponent implements OnInit {
  private readonly http = inject(HttpClient);

  jobs = signal<Job[]>([]);
  isLoading = signal(true);
  error = signal('');

  ngOnInit(): void {
    this.http.get<Job[]>('http://localhost:8080/api/jobs').subscribe({
      next: (data) => {
        this.jobs.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Unable to load jobs. Please try again later.');
        this.isLoading.set(false);
      }
    });
  }
}
