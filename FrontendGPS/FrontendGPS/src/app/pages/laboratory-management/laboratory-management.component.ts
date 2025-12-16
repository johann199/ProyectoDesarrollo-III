import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../core/services/api.service';
import { apiConstants } from '../../core/constants/api.constants';
import { Laboratory, LaboratoryRequest } from '../../core/interfaces/laboratory.interface';

@Component({
  selector: 'app-laboratory-management',
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
    MatDialogModule
  ],
  templateUrl: './laboratory-management.component.html',
  styleUrl: './laboratory-management.component.scss'
})
export class LaboratoryManagementComponent implements OnInit {
  @ViewChild('deactivateDialog') deactivateDialog!: TemplateRef<any>;

  laboratoryForm: FormGroup;
  isLoading = false;
  isLoadingList = false;
  
  laboratoriesList: Laboratory[] = [];
  displayedColumns: string[] = ['id', 'nombre', 'capacidad', 'estado', 'acciones'];
  
  totalItems = 0;
  pageSize = 10;
  currentPage = 0;

  selectedLaboratory: Laboratory | null = null;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.laboratoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      capacity: ['', [Validators.required, Validators.min(1), Validators.max(1000)]]
    });
  }

  ngOnInit() {
    this.loadLaboratories();
  }

  loadLaboratories() {
    this.isLoadingList = true;
    
    const params = {
      page: this.currentPage.toString(),
      size: this.pageSize.toString(),
      sort: 'name,asc'
    };

    this.apiService.get<any>(apiConstants.LABORATORIES, params).subscribe({
      next: (response) => {
        this.isLoadingList = false;
        
        if (Array.isArray(response)) {
          this.laboratoriesList = response;
          this.totalItems = response.length;
        } else if (response.content) {
          this.laboratoriesList = response.content;
          this.totalItems = response.totalElements;
        } else {
          this.laboratoriesList = [response];
          this.totalItems = 1;
        }
      },
      error: (error) => {
        this.isLoadingList = false;
        this.showSnackBar('Error al cargar los laboratorios', 'error');
      }
    });
  }

  onCreateLaboratory() {
    if (this.laboratoryForm.invalid) {
      this.laboratoryForm.markAllAsTouched();
      return;
    }

    const laboratoryData: LaboratoryRequest = {
      name: this.laboratoryForm.get('name')?.value?.trim(),
      capacity: parseInt(this.laboratoryForm.get('capacity')?.value)
    };

    this.isLoading = true;

    this.apiService.post(apiConstants.LABORATORIES, laboratoryData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.showSnackBar('Laboratorio creado exitosamente', 'success');
        this.resetForm();
        this.loadLaboratories();
      },
      error: (error) => {
        this.isLoading = false;
        
        if (error.message?.includes('409') || error.status === 409) {
          this.showSnackBar('Ya existe un laboratorio con ese nombre', 'error');
        } else if (error.message?.includes('400') || error.status === 400) {
          this.showSnackBar('Datos inválidos. Verifique la información', 'error');
        } else {
          this.showSnackBar('Error al crear el laboratorio: ' + error.message, 'error');
        }
      }
    });
  }

  openDeactivateDialog(laboratory: Laboratory) {
    this.selectedLaboratory = laboratory;
    this.dialog.open(this.deactivateDialog, {
      width: '500px',
      panelClass: 'custom-dialog'
    });
  }

  closeDialog() {
    this.dialog.closeAll();
    this.selectedLaboratory = null;
  }

  confirmDeactivate() {
    if (this.selectedLaboratory) {
      this.apiService.put(`${apiConstants.LABORATORIES}/${this.selectedLaboratory.id}/deactivate`, {}).subscribe({
        next: () => {
          this.showSnackBar('Laboratorio desactivado exitosamente', 'success');
          this.closeDialog();
          this.loadLaboratories();
        },
        error: (error) => {
          if (error.status === 404) {
            this.showSnackBar('Laboratorio no encontrado', 'error');
          } else {
            this.showSnackBar('Error al desactivar el laboratorio: ' + error.message, 'error');
          }
          this.closeDialog();
        }
      });
    }
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadLaboratories();
  }

  private resetForm() {
    this.laboratoryForm.reset();
    this.laboratoryForm.markAsPristine();
    this.laboratoryForm.markAsUntouched();
  }

  private showSnackBar(message: string, type: 'success' | 'error') {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      panelClass: type === 'success' ? 'snackbar-success' : 'snackbar-error'
    });
  }

  getStatusBadge(active: boolean): string {
    return active ? 'Activo' : 'Inactivo';
  }

  getStatusClass(active: boolean): string {
    return active ? 'status-active' : 'status-inactive';
  }
}