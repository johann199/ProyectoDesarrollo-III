import { Routes } from '@angular/router';
import { LoginComponent } from './auth/pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { AttendanceRegisterComponent } from './pages/attendance-register/attendance-register.component';
import { LaboratoryManagementComponent } from './pages/laboratory-management/laboratory-management.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { ReservationsAdminComponent } from './pages/reservations-admin/reservations-admin.component';
import { LoanManagementComponent } from './pages/loan-management/loan-management.component';


export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'gestion-usuarios', component: UserManagementComponent },
  { path: 'registro-asistencias', component: AttendanceRegisterComponent },
  { path: 'gestion-laboratorios', component: LaboratoryManagementComponent },
  { path: 'servicios-practicas', component: ReservationsAdminComponent },
  { path: 'prestamo-equipos', component: LoanManagementComponent },
  { path: '**', redirectTo: 'login' }
];
