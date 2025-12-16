import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTabsModule } from '@angular/material/tabs';
import { ApiService } from '../../core/services/api.service';
import { apiConstants } from '../../core/constants/api.constants';
import { LoanRequest, LoanResponse } from '../../core/interfaces/loan.interface';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-loan-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatPaginatorModule,
    MatTabsModule
  ],
  templateUrl: './loan-management.component.html',
  styleUrl: './loan-management.component.scss'
})
export class LoanManagementComponent implements OnInit {
  loanForm: FormGroup;
  returnForm: FormGroup;
  
  isLoadingLoan = false;
  isLoadingReturn = false;
  isLoadingLoans = false;
  
  activeLoans: LoanResponse[] = [];
  myActiveLoans: LoanResponse[] = [];
  
  displayedColumns: string[] = ['id', 'equipment', 'student', 'monitor', 'loanDate', 'actions'];
  myLoansDisplayedColumns: string[] = ['id', 'equipment', 'loanDate', 'status'];
 
  totalItems = 0;
  pageSize = 10;
  currentPage = 0;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.loanForm = this.fb.group({
      barcode: ['', [Validators.required]],
      studentCode: ['', [Validators.required]]
    });

    this.returnForm = this.fb.group({
      loanId: ['', [Validators.required, Validators.pattern('^[0-9]+$')]]
    });
  }

  ngOnInit() {
    this.loadActiveLoans();
  }


  onCreateLoan() {
  if (this.loanForm.invalid) {
    this.loanForm.markAllAsTouched();
    return;
  }


  const currentUser = this.authService.getCurrentUser();
  
  if (!currentUser || !currentUser.id) {
    this.showSnackBar('No se pudo identificar al monitor. Por favor, inicie sesión nuevamente.', 'error');
    return;
  }

  const loanData: LoanRequest = {
    barcode: this.loanForm.get('barcode')?.value?.trim(),
    studentCode: this.loanForm.get('studentCode')?.value?.trim(),
    monitorCode: currentUser.id 
  };

  //console.log('Datos del préstamo:', loanData); 

  this.isLoadingLoan = true;

  this.apiService.post(apiConstants.LOANS, loanData).subscribe({
    next: (response) => {
      this.isLoadingLoan = false;
      this.showSnackBar('Préstamo registrado exitosamente', 'success');
      this.loanForm.reset();
      this.loadActiveLoans();
    },
    error: (error) => {
      this.isLoadingLoan = false;
      this.handleLoanError(error);
    }
  });
}


  onReturnLoan() {
    if (this.returnForm.invalid) {
      this.returnForm.markAllAsTouched();
      return;
    }

    const loanId = this.returnForm.get('loanId')?.value;
    this.isLoadingReturn = true;

    this.apiService.post(`${apiConstants.LOANS}/${loanId}/return`, {}).subscribe({
      next: (response) => {
        this.isLoadingReturn = false;
        this.showSnackBar('Equipo devuelto exitosamente', 'success');
        this.returnForm.reset();
        this.loadActiveLoans();
      },
      error: (error) => {
        this.isLoadingReturn = false;
        this.handleReturnError(error);
      }
    });
  }


  returnLoanFromTable(loanId: number) {
    this.isLoadingReturn = true;

    this.apiService.post(`${apiConstants.LOANS}/${loanId}/return`, {}).subscribe({
      next: (response) => {
        this.isLoadingReturn = false;
        this.showSnackBar('Equipo devuelto exitosamente', 'success');
        this.loadActiveLoans();
      },
      error: (error) => {
        this.isLoadingReturn = false;
        this.handleReturnError(error);
      }
    });
  }

  
  loadActiveLoans() {
    this.isLoadingLoans = true;

    
    this.apiService.get<LoanResponse[]>(`${apiConstants.LOANS}/active`).subscribe({
      next: (response) => {
        this.activeLoans = Array.isArray(response) ? response : [response];
        this.isLoadingLoans = false;
      },
      error: (error) => {
        this.isLoadingLoans = false;
        this.showSnackBar('Error al cargar los préstamos activos', 'error');
      }
    });

    
    this.apiService.get<LoanResponse[]>(`${apiConstants.LOANS}/my-active`).subscribe({
      next: (response) => {
        this.myActiveLoans = Array.isArray(response) ? response : [response];
      },
      error: (error) => {
        console.log('No se pudieron cargar los préstamos personales');
      }
    });
  }


  private handleLoanError(error: any) {
    if (error.status === 409) {
      this.showSnackBar('El equipo no tiene unidades disponibles', 'error');
    } else if (error.status === 404) {
      this.showSnackBar('Equipo o estudiante no encontrado', 'error');
    } else if (error.status === 403) {
      this.showSnackBar('No tiene permisos para realizar préstamos', 'error');
    } else {
      this.showSnackBar('Error al registrar el préstamo: ' + error.message, 'error');
    }
  }

  private handleReturnError(error: any) {
    if (error.status === 404) {
      this.showSnackBar('Préstamo no encontrado', 'error');
    } else if (error.status === 400) {
      this.showSnackBar('Este préstamo ya fue devuelto', 'error');
    } else if (error.status === 403) {
      this.showSnackBar('No tiene permisos para procesar devoluciones', 'error');
    } else {
      this.showSnackBar('Error al procesar la devolución: ' + error.message, 'error');
    }
  }


  private getCurrentUserCode(): string {
    const userData = localStorage.getItem('userData');
    if (userData) {
      const user = JSON.parse(userData);
      return user.code || '';
    }
    return '';
  }

  private showSnackBar(message: string, type: 'success' | 'error') {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      panelClass: type === 'success' ? 'snackbar-success' : 'snackbar-error'
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('es-CO');
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadActiveLoans();
  }
}