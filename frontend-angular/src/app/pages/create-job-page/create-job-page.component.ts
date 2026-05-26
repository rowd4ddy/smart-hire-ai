import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { JobService } from '../../services/job.service';

@Component({
  selector: 'app-create-job-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './create-job-page.component.html'
})
export class CreateJobPageComponent {
  private readonly jobService = inject(JobService);
  private readonly router = inject(Router);

  title = '';
  company = '';
  location = '';
  department = '';
  employmentType = 'Full-time';
  workMode = 'Hybrid';
  salaryRange = '';
  applicationDeadline = '';
  status = 'Open';
  skillsInput = '';
  minimumExperienceYears: number | null = null;
  educationLevel = 'Bachelor';
  chatInput = '';

  isSubmitting = signal(false);
  error = signal('');
  success = signal(false);
  showCreationChoice = signal(true);
  creationMode = signal<'form' | 'chat' | null>(null);
  chatMessages = signal([
    {
      role: 'assistant',
      text: 'Hi, I can help you shape a job post. Tell me the role, location, must-have skills, and seniority. This is a design preview for now, so I will not call a backend yet.'
    }
  ]);

  readonly educationOptions = ['None', 'High School', 'Associate', 'Bachelor', 'Master', 'PhD'];
  readonly employmentTypeOptions = ['Full-time', 'Part-time', 'Contract', 'Internship'];
  readonly workModeOptions = ['On-site', 'Hybrid', 'Remote'];
  readonly statusOptions = ['Open', 'Draft', 'Paused'];

  get parsedSkills(): string[] {
    return this.skillsInput.split(',').map((skill) => skill.trim()).filter((skill) => skill.length > 0);
  }

  useForm(): void {
    this.creationMode.set('form');
    this.showCreationChoice.set(false);
  }

  useChatbot(): void {
    this.creationMode.set('chat');
    this.showCreationChoice.set(false);
  }

  reopenChoice(): void {
    this.showCreationChoice.set(true);
  }

  sendChatMessage(): void {
    const text = this.chatInput.trim();
    if (!text) {
      return;
    }

    this.chatMessages.update((messages) => [
      ...messages,
      { role: 'recruiter', text },
      {
        role: 'assistant',
        text: 'Draft noted. Once the AI service is connected, I will turn this into title, requirements, salary, deadline, and screening criteria automatically. For now, use the form below to post it.'
      }
    ]);
    this.chatInput = '';
  }

  submit(): void {
    this.error.set('');
    if (!this.title.trim() || !this.company.trim() || this.parsedSkills.length === 0) {
      this.error.set('Job title, company, and at least one required skill are required.');
      return;
    }

    this.isSubmitting.set(true);
    const payload = {
      title: this.title.trim(),
      company: this.company.trim(),
      requiredSkills: this.parsedSkills,
      minimumExperienceYears: this.minimumExperienceYears ?? 0,
      educationLevel: this.educationLevel,
      location: this.location.trim(),
      department: this.department.trim(),
      employmentType: this.employmentType,
      workMode: this.workMode,
      salaryRange: this.salaryRange.trim(),
      applicationDeadline: this.applicationDeadline || null,
      status: this.status
    };

    this.jobService.createJob(payload).subscribe({
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
