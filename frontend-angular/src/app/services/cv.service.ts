import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface CandidateProfile {
  id: number;
  fullName: string;
  email: string;
  skills: string[];
  experienceYears: number;
  educationLevel: string;
}

@Injectable({
  providedIn: 'root'
})
export class CvService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/candidates`;

  getCandidateProfile(): Observable<CandidateProfile> {
    return this.http.get<CandidateProfile>(`${this.apiUrl}/me`);
  }
}
