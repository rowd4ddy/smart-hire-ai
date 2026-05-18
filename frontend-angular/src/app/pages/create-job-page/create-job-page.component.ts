import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-create-job-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './create-job-page.component.html'
})
export class CreateJobPageComponent {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  title = '';
  company = '';
  skillsInput = '';
  minimumExperienceYears: number | null = null;
  educationLevel = 'Bachelor';

  isSubmitting = signal(false);
  error = signal('');
  success = signal(false);

  readonly educationOptions = ['None', 'High School', 'Associate', 'Bachelor', 'Master', 'PhD'];

  get parsedSkills(): string[] {
    return this.skillsInput.split(',').map((skill) => skill.trim()).filter((skill) => skill.length > 0);
  }

  submit(): void {
    this.error.set('');
    if (!this.title.trim() || !this.company.trim() || this.parsedSkills.length === 0) {
      this.error.set('Title, company, and at least one skill are required.');
      return;
    }

    this.isSubmitting.set(true);
    const payload = {
      title: this.title.trim(),
      company: this.company.trim(),
      requiredSkills: this.parsedSkills,
      minimumExperienceYears: this.minimumExperienceYears ?? 0,
      educationLevel: this.educationLevel
    };

    this.http.post('http://localhost:8080/api/jobs', payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/recruiter']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.error.set(err.error?.message ?? 'Failed to create job. Please try again.');
      }
    });
  }
}
