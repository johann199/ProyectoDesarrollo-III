import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface User {
  id: string;
  name: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    // Verificar si hay datos de usuario guardados al inicializar
    this.checkStoredUser();
  }

  login(userData: User, token: string): void {
    // Guardar datos en localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('userData', JSON.stringify(userData));
    
    // Actualizar el estado actual
    this.currentUserSubject.next(userData);
  }

  logout(): void {
    // Limpiar localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userData');
    
    // Actualizar el estado
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('userData');
    return !!(token && userData);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  private checkStoredUser(): void {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('userData');
    
    if (token && userData) {
      try {
        const user = JSON.parse(userData);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Error parsing user data:', error);
        this.logout();
      }
    }
  }
}
