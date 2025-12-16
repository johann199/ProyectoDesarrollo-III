import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  socialLinks = [
    { icon: 'facebook', url: '#', label: 'Facebook' },
    { icon: 'twitter', url: '#', label: 'Twitter' },
    { icon: 'linkedin', url: '#', label: 'LinkedIn' },
    { icon: 'instagram', url: '#', label: 'Instagram' }
  ];

  quickLinks = [
    { label: 'Inicio', route: '/dashboard' },
    { label: 'Vehículos', route: '/vehicles' },
    { label: 'Rutas', route: '/routes' },
    { label: 'Reportes', route: '/reports' }
  ];

  companyLinks = [
    { label: 'Acerca de', route: '/about' },
    { label: 'Contacto', route: '/contact' },
    { label: 'Política de Privacidad', route: '/privacy' },
    { label: 'Términos de Servicio', route: '/terms' }
  ];
}
