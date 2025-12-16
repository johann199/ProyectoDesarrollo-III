import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { apiConstants } from '../constants/api.constants';
import { UserRole } from '../constants/roles.constants';
import { User } from '../interfaces/role.interface';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) {}

  /**
   * Assign a role to a user.
   * According to the API (Swagger) the endpoint is: PUT /api/users/{userCode}/role?roleName=ROLE
   * So we use PUT and pass roleName as a query parameter.
   */
  assignRole(userCode: string, role: UserRole): Observable<void> {
    const url = apiConstants.ASSIGN_USER_ROLE.replace(':userCode', userCode);
    // Append roleName as query param to match Swagger contract
    const endpointWithQuery = `${url}?roleName=${encodeURIComponent(role)}`;
    return this.apiService.put<void>(endpointWithQuery, {});
  }

  getUsers(): Observable<User[]> {
    const url = apiConstants.LIST_USER;
    return this.apiService.get<User[]>(url);
  }
}