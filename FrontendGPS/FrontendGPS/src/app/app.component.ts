import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from './shared/navigation/navigation.component';
import { FooterComponent } from './shared/footer/footer.component';
import { AuthService } from './auth/services/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    CommonModule,
    NavigationComponent,
    FooterComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'GPS System';
  isLoggedIn = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Verificar si el usuario está logueado
    this.checkAuthStatus();
    
    // Escuchar cambios de ruta para actualizar el estado
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.checkAuthStatus();
      });
  }

  checkAuthStatus() {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('userData');
    this.isLoggedIn = !!(token && userData);
  }
}
