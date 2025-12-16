import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatGridListModule,
    MatProgressBarModule,
    MatChipsModule,
    MatTableModule,
    MatTabsModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
    stats = [
    {
      title: 'Total Solicitudes',
      value: '24',
      change: '+2',
      changeType: 'positive',
      icon: 'request_quote',
      color: 'primary'
    },
    {
      title: 'Pendientes de Revisión',
      value: '8',
      change: '+1',
      changeType: 'positive',
      icon: 'pending_actions',
      color: 'warn'
    },
    {
      title: 'Aprobadas',
      value: '1,247',
      change: '+156',
      changeType: 'positive',
      icon: 'verified',
      color: 'accent'
    },
    {
      title: 'Rechazadas',
      value: '3',
      change: '-1',
      changeType: 'negative',
      icon: 'assistant_direction',
      color: 'alert'
    }
  ];

  

  alerts = [
    { type: 'warning', message: 'Sin stock', time: '5 min' },
    { type: 'error', message: 'Error en el sistema', time: '15 min' },
    { type: 'info', message: 'Nueva practica', time: '30 min' }
  ];



  ngOnInit() {
    
  }


  getAlertIcon(type: string): string {
    switch (type) {
      case 'warning': return 'warning';
      case 'error': return 'error';
      case 'info': return 'info';
      default: return 'notifications';
    }
  }
}
