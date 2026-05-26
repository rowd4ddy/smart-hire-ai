import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface Job {
  id: number;
  title: string;
  company: string;
  requiredSkills: string[];
  minimumExperienceYears: number;
  educationLevel: string;
  location: string;
  department: string;
  employmentType: string;
  workMode: string;
  salaryRange: string;
  applicationDeadline: string;
  status: string;
}

export interface CreateJobPayload {
  title: string;
  company: string;
  requiredSkills: string[];
  minimumExperienceYears: number;
  educationLevel: string;
  location: string;
  department: string;
  employmentType: string;
  workMode: string;
  salaryRange: string;
  applicationDeadline: string | null;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class JobService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/jobs`;

  getJobs(): Observable<Job[]> {
    return this.http.get<Job[]>(this.apiUrl);
  }

  createJob(payload: CreateJobPayload): Observable<Job> {
    return this.http.post<Job>(this.apiUrl, payload);
  }
}
