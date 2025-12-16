import { Component, Input } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-field-error',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div *ngIf="control && control.errors && control.touched" class="error-message">
      <span *ngIf="control.errors['required']">Campo requerido</span>
      <span *ngIf="control.errors['pattern']">Formato inválido</span>
      <span *ngIf="control.errors['minlength']">Formato inválido</span>
      <span *ngIf="control.errors['email']">Formato inválido</span>
      <span *ngIf="control.errors['min'] || control.errors['max']">Formato inválido</span>
    </div>
  `,
  styles: []
})
export class FieldErrorComponent {
  @Input() control: AbstractControl | null = null;
}
