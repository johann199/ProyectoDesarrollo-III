import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AVAILABLE_ROLES, UserRole } from '../../../core/constants/roles.constants';

@Component({
  selector: 'app-add-user-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './add-user-dialog.component.html',
  styleUrls: ['./add-user-dialog.component.scss']
})
export class AddUserDialogComponent {
  roleAssignment: { userCode: string; role: UserRole } = {
    userCode: '',
    role: null as unknown as UserRole
  };

  availableRoles = AVAILABLE_ROLES;

  constructor(
    public dialogRef: MatDialogRef<AddUserDialogComponent>
  ) {}

  isFormValid(): boolean {
    return !!(this.roleAssignment.userCode && this.roleAssignment.role);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.isFormValid()) {
      this.dialogRef.close(this.roleAssignment);
    }
  }
}