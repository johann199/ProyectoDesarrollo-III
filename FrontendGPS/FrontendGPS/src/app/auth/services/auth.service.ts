import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, tap } from 'rxjs';
import { TokenService } from '../../core/services/token.service';
import { apiConstants } from '../../core/constants/api.constants';
import { User } from '../../core/types/user.type';
import { Router } from '@angular/router';

interface LoginResponse {
  token: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private readonly _http: HttpClient, 
    private readonly _tokenService: TokenService,
    private readonly _router: Router
  ) { 
    // Inicializar el estado de autenticación al cargar el servicio
    this.initializeAuthState();
  }

  private initializeAuthState(): void {
    const token = this._tokenService.getToken();
    if (token && !this._tokenService.isTokenExpired(token)) {
      try {
        const user = this._tokenService.decodeToken(token);
        if (user.name) {
          user.name = this.decodeSpecialCharacters(user.name);
        }
        this.currentUserSubject.next(user);
      } catch (error) {
        this._tokenService.clear();
        localStorage.removeItem('userData');
      }
    }
  }

  login(username: string, password: string): Observable<User> {
    return this._http.post<LoginResponse>(`${apiConstants.BASE_URL}${apiConstants.LOGIN}`, { code: username, password })
      .pipe(
        map((response: LoginResponse) => {
          this._tokenService.setToken(response.token);
          const user = this._tokenService.decodeToken(response.token);
          
          // Decodificar caracteres especiales en el nombre
          if (user.name) {
            user.name = this.decodeSpecialCharacters(user.name);
          }
          
          // Guardar datos del usuario en localStorage para la navegación
          localStorage.setItem('userData', JSON.stringify(user));
          this.currentUserSubject.next(user);
          
          return user;
        }),
        tap((user) => {
          // Redirigir al dashboard después del login exitoso
          this._router.navigate(['/dashboard']);
        })
      );
  }

  logout(): void {
    this._tokenService.clear();
    localStorage.removeItem('userData');
    this.currentUserSubject.next(null);
    this._router.navigate(['/login']);
  }

  // Método isAuthenticated que faltaba
  isAuthenticated(): boolean {
    const token = this._tokenService.getToken();
    return !!token && !this._tokenService.isTokenExpired(token);
  }

  // Obtener usuario actual
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  // Verificar si el token está expirado
  isTokenValid(): boolean {
    const token = this._tokenService.getToken();
    return !!token && !this._tokenService.isTokenExpired(token);
  }

  private decodeSpecialCharacters(text: string): string {
    if (!text) return '';
    
    // Decodificar caracteres especiales comunes
    return text
      .replace(/Ã±/g, 'ñ')
      .replace(/Ã¡/g, 'á')
      .replace(/Ã©/g, 'é')
      .replace(/Ã­/g, 'í')
      .replace(/Ã³/g, 'ó')
      .replace(/Ãº/g, 'ú')
      .replace(/Ã/g, 'Á')
      .replace(/Ã‰/g, 'É')
      .replace(/Ã/g, 'Í')
      .replace(/Ã"/g, 'Ó')
      .replace(/Ãš/g, 'Ú')
      .replace(/Ã¼/g, 'ü')
      .replace(/Ã‡/g, 'Ç');
  }
}