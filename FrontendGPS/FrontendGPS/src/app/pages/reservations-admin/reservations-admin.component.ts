import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ScheduleFormComponent } from './schedule-form/schedule-form.component';
import { PracticeService, Practice } from '../../core/services/practice.service';

@Component({
  selector: 'app-reservations-admin',
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
  templateUrl: './reservations-admin.component.html',
  styleUrls: ['./reservations-admin.component.scss']
})
export class ReservationsAdminComponent implements OnInit {
  availableTimes = [
    { date: '14/10/2025', time: '08:00 - 10:00', capacity: '5/15', status: 'available', color: 'green' },
    { date: '14/10/2025', time: '08:00 - 10:00', capacity: '5/15', status: 'full', color: 'red' },
    { date: '14/10/2025', time: '08:00 - 10:00', capacity: '5/15', status: 'available', color: 'yellow' },
    { date: '14/10/2025', time: '12:00 - 14:00', capacity: '5/15', status: 'available', color: 'green' },
  ];

  reservations: Practice[] = [];

  constructor(private dialog: MatDialog, private practiceService: PracticeService) {}

  ngOnInit(): void {
    this.practiceService.getMyPractices().subscribe({
      next: (response: any) => {
        this.reservations = response.content;
      },
      error: (err: any) => {
        // Puedes mostrar un mensaje de error aquí
        console.error('Error al cargar prácticas:', err);
      }
    });
  }

  reserveSlot(slot: any) {
    if (slot.status !== 'full') {
      console.log('Reservando...', slot);
    }
  }

  openScheduleForm() {
    const dialogRef = this.dialog.open(ScheduleFormComponent, {
      width: '500px',
      height: 'auto',         // Tamaño ideal
      maxWidth: '95vw',        // Responsive
      disableClose: true,
      autoFocus: true,
      role: 'dialog',
      ariaLabel: 'Formulario para agendar práctica'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log(result);
        // Recargar las prácticas después de crear una nueva
        this.ngOnInit();
      }
    });
  }

  // Función para trackBy en *ngFor (mejora rendimiento)
  trackByPracticeId(index: number, practice: Practice): number {
    return practice.id;
  }
}