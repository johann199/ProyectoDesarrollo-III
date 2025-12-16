import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { UserService } from '../../core/services/user.service';
import { AddUserDialogComponent } from './add-user-dialog/add-user-dialog.component';
import { User } from '../../core/interfaces/role.interface';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatGridListModule,
    MatProgressBarModule,
    MatChipsModule,
    MatTableModule,
    MatTabsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  searchTerm: string = '';
  selectedRoleFilter: string = '';
  selectedStatusFilter: string = '';

  statistics = {
    totalUsers: 0,
    students: 0,
    teachers: 0,
    admins: 0
  };

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.updateStatistics();
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.snackBar.open('Error al cargar los usuarios', 'Cerrar', {
          duration: 3000
        });
      }
    });
  }

  updateStatistics(): void {
    this.statistics.totalUsers = this.users.length;
    this.statistics.students = this.users.filter(user => user.role.name === 'ESTUDIANTE').length;
    this.statistics.teachers = this.users.filter(user => user.role.name === 'DOCENTE').length;
    this.statistics.admins = this.users.filter(user => user.role.name === 'ADMIN').length;
  }

  get filteredUsers(): User[] {
    return this.users.filter(user => {
      const matchesSearch = this.searchTerm ? 
        user.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.code.toLowerCase().includes(this.searchTerm.toLowerCase()) :
        true;

      const matchesRole = this.selectedRoleFilter ?
        user.role.name === this.selectedRoleFilter :
        true;

      // Ya no filtramos por status ya que no viene en la respuesta del backend
      return matchesSearch && matchesRole;
    });
  }

  onStatusFilterChange(event: Event): void {
    this.selectedStatusFilter = (event.target as HTMLSelectElement).value;
  }

  openAssignRoleDialog(): void {
    const dialogRef = this.dialog.open(AddUserDialogComponent, {
      width: '420px',
      maxHeight: '90vh',
      autoFocus: false,
      panelClass: 'custom-popup-dialog',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.assignRole(result.userCode, result.role).subscribe({
          next: () => {
            this.snackBar.open('Rol asignado correctamente', 'Cerrar', {
              duration: 3000,
              horizontalPosition: 'end',
              verticalPosition: 'top'
            });
            this.loadUsers();
          },
          error: (error) => {
            this.snackBar.open('Error al asignar el rol', 'Cerrar', {
              duration: 3000,
              horizontalPosition: 'end',
              verticalPosition: 'top'
            });
            console.error('Error al asignar rol:', error);
          }
        });
      }
    });
  }

  // Método simple para búsqueda
  onSearch(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value;
  }

  // Método simple para cambio de filtro de rol
  onRoleFilterChange(event: Event): void {
    this.selectedRoleFilter = (event.target as HTMLSelectElement).value;
  }
}