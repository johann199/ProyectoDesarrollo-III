import { Component, Input } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-field-error',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './field-error.component.html',
  styleUrls: ['./field-error.component.scss']
})
export class FieldErrorComponent {
  @Input() control: AbstractControl | null = null;
}
