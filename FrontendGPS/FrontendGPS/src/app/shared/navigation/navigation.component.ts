import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { AuthService } from '../../auth/services/auth.service';
import { User } from '../../core/types/user.type';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatSidenavModule,
    MatListModule
  ],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent implements OnInit, OnDestroy {
  isMenuOpen = true; // Abierto por defecto
  isLoggedIn = false;
  userRole = '';
  userName = '';
  menuItems: any[] = [];
  private userSubscription: Subscription = new Subscription();

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Verificar si el usuario está logueado
    this.checkAuthStatus();
    
    // Ajustar el estado del menú según el tamaño de pantalla
    this.adjustMenuForScreenSize();
    
    // Escuchar cambios de tamaño de pantalla
    window.addEventListener('resize', () => {
      this.adjustMenuForScreenSize();
    });
  }

  ngOnDestroy() {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  checkAuthStatus() {
    // Verificar si el usuario está autenticado
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('userData');
    
    if (token && userData) {
      this.isLoggedIn = true;
      const user = JSON.parse(userData);
      this.userRole = user.role;
      // Decodificar caracteres especiales correctamente
      this.userName = this.decodeSpecialCharacters(user.name);
      this.menuItems = this.getMenuItems();
    } else {
      this.isLoggedIn = false;
      this.userRole = '';
      this.userName = '';
      this.menuItems = [];
    }
  }

  decodeSpecialCharacters(text: string): string {
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

  adjustMenuForScreenSize() {
    if (window.innerWidth > 768) {
      // Desktop: sidebar abierto por defecto
      this.isMenuOpen = true;
    } else {
      // Mobile: sidebar cerrado por defecto
      this.isMenuOpen = false;
    }
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }


  getMenuItems() {
    switch (this.userRole) {
      case 'ESTUDIANTE':
        return [
          { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
          { label: 'Agendar Práctica', route: '/agendar-practica', icon: 'event' },
          { label: 'Equipos del Laboratorio', route: '/equipos', icon: 'science' }
        ];
      case 'ADMIN':
        return [
          { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
          { label: 'Gestión de Usuarios', route: '/gestion-usuarios', icon: 'people' },
          { label: 'Gestión de Laboratorios', route: '/gestion-laboratorios', icon: 'science' },
          { label: 'Registro Asistencias', route: '/registro-asistencias', icon: 'checklist' },
          { label: 'Servicios de Prácticas', route: '/servicios-practicas', icon: 'assignment' },
          { label: 'Préstamo de Equipos', route: '/prestamo-equipos', icon: 'inventory' }
        ];
      
      case 'DOCENTE':
        return [
          { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
          { label: 'Registro Asistencias', route: '/registro-asistencias', icon: 'checklist' },
          { label: 'Servicios de Prácticas', route: '/servicios-practicas', icon: 'assignment' },
      ];
      
      case 'MONITOR':
        return [
          { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
          { label: 'Registro Asistencias', route: '/registro-asistencias', icon: 'checklist' },
          { label: 'Préstamo de Equipos', route: '/prestamo-equipos', icon: 'inventory' }
        ];
      default:
        return [];
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
    this.isMenuOpen = false;
  }

  closeMenuOnMobile() {
    // En dispositivos móviles, cerrar el menú después de navegar
    if (window.innerWidth <= 768) {
      this.isMenuOpen = false;
    }
  }
}
