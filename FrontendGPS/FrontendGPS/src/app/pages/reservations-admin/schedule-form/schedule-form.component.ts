import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogActions, MatDialogContent } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { PracticeService, PracticeRequest } from '../../../core/services/practice.service';

@Component({
  selector: 'app-schedule-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDialogActions,
    MatDialogContent
  ],
  templateUrl: './schedule-form.component.html',
  styleUrls: ['./schedule-form.component.scss']
})
export class ScheduleFormComponent {

  form = new FormGroup({
    subject: new FormControl('', [Validators.required]),
    laboratoryName: new FormControl('', [Validators.required]),
    practiceType: new FormControl('ELECTRONICA', [Validators.required]),
    date: new FormControl('', [Validators.required]),
    startTime: new FormControl('', [Validators.required]),
    durationMinutes: new FormControl(30, [Validators.required, Validators.min(1)]),
    studentCount: new FormControl(1, [Validators.required, Validators.min(1)])
  });

  constructor(
    private dialogRef: MatDialogRef<ScheduleFormComponent>,
    private practiceService: PracticeService
  ) {}

  submit() {
    if (this.form.valid) {
      const payload: PracticeRequest = this.form.value as PracticeRequest;
      this.practiceService.createPractice(payload).subscribe({
        next: (response: any) => {
          // Puedes mostrar un mensaje de éxito aquí
          this.dialogRef.close(response);
        },
        error: (error: any) => {
          // Puedes mostrar un mensaje de error aquí
          this.form.setErrors({ submit: 'Error al crear la práctica' });
        }
      });
    } else {
      this.form.markAllAsTouched();
    }
  }

  cancel() {
    this.dialogRef.close();
  }
}