import { Routes, Router } from '@angular/router';
import { inject } from '@angular/core';
import { HomeLayoutComponent } from './layouts/home-layout/home-layout.component';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { RecruiterDashboardPageComponent } from './pages/recruiter-dashboard-page/recruiter-dashboard-page.component';
import { CandidateDashboardPageComponent } from './pages/candidate-dashboard-page/candidate-dashboard-page.component';
import { JobsPageComponent } from './pages/jobs-page/jobs-page.component';
import { CreateJobPageComponent } from './pages/create-job-page/create-job-page.component';
import { AccountSettingsPageComponent } from './pages/account-settings-page/account-settings-page.component';
import { CandidateProfilePageComponent } from './pages/candidate-profile-page/candidate-profile-page.component';
import { LoginPageComponent } from './pages/login-page/login-page.component';
import { RegisterPageComponent } from './pages/register-page/register-page.component';
import { AuthService } from './services/auth.service';
import { roleGuard } from './guards/role.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    component: HomeLayoutComponent,
    children: [
      {
        path: '',
        component: HomePageComponent
      }
    ]
  },
  {
    path: 'login',
    component: LoginPageComponent,
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    component: RegisterPageComponent,
    canActivate: [guestGuard]
  },
  {
    path: 'jobs',
    component: JobsPageComponent
  },
  {
    path: 'settings',
    component: AccountSettingsPageComponent,
    canActivate: [() => {
      const auth = inject(AuthService);
      const router = inject(Router);
      return auth.isLoggedIn() ? true : router.createUrlTree(['/login']);
    }]
  },
  {
    path: 'candidate/profile',
    component: CandidateProfilePageComponent,
    canActivate: [roleGuard],
    data: { roles: ['CANDIDATE'] }
  },
  {
    path: 'recruiter/jobs/new',
    component: CreateJobPageComponent,
    canActivate: [roleGuard],
    data: { roles: ['RECRUITER'] }
  },
  {
    path: 'recruiter',
    component: RecruiterDashboardPageComponent,
    canActivate: [roleGuard],
    data: { roles: ['RECRUITER'] }
  },
  {
    path: 'candidate',
    component: CandidateDashboardPageComponent,
    canActivate: [roleGuard],
    data: { roles: ['CANDIDATE'] }
  }
];
