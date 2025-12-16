import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { apiConstants } from '../constants/api.constants';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = apiConstants.BASE_URL;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  get<T>(endpoint: string, params?: { [key: string]: string | string[] }): Observable<T> {
    const url = `${this.baseUrl}${endpoint}`;
    let httpParams = new HttpParams();
    
    if (params) {
      Object.keys(params).forEach(key => {
        const value = params[key];
        if (value !== null && value !== undefined && value !== '') {
          if (Array.isArray(value)) {
            value.forEach(v => httpParams = httpParams.append(key, v));
          } else {
            httpParams = httpParams.set(key, value);
          }
        }
      });
    }
    
    return this.http.get<T>(url, {
      headers: this.getHeaders(),
      params: httpParams
    }).pipe(
      catchError(this.handleError)
    );
  }

  post<T>(endpoint: string, data: any = {}): Observable<T> {
    const url = `${this.baseUrl}${endpoint}`;
    return this.http.post<T>(url, data, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ AGREGAR ESTE MÉTODO PUT
  put<T>(endpoint: string, data: any = {}): Observable<T> {
    const url = `${this.baseUrl}${endpoint}`;
    return this.http.put<T>(url, data, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    let errorMessage = 'An unexpected error occurred';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }
    
    return throwError(() => ({ 
      message: errorMessage,
      status: error.status,
      error: error.error 
    }));
  }
}