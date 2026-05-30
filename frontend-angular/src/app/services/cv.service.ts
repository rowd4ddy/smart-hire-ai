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

export interface CvVersion {
  id: number;
  fileName: string;
  fileUrl: string;
  fileType: string;
  fileSize: number;
  versionNumber: number;
  active: boolean;
  uploadedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CvService {
  private readonly http = inject(HttpClient);
  private readonly profileApiUrl = `${API_BASE_URL}/candidates`;
  private readonly cvApiUrl = `${API_BASE_URL}/candidate/cvs`;

  getCandidateProfile(): Observable<CandidateProfile> {
    return this.http.get<CandidateProfile>(`${this.profileApiUrl}/me`);
  }

  getCvVersions(): Observable<CvVersion[]> {
    return this.http.get<CvVersion[]>(this.cvApiUrl);
  }

  uploadCv(file: File): Observable<CvVersion> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CvVersion>(this.cvApiUrl, formData);
  }
}
