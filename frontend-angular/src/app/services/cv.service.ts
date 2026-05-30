import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

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
  private readonly apiUrl = `${API_BASE_URL}/candidate/cvs`;

  getCvVersions(): Observable<CvVersion[]> {
    return this.http.get<CvVersion[]>(this.apiUrl);
  }

  uploadCv(file: File): Observable<CvVersion> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CvVersion>(this.apiUrl, formData);
  }
}
