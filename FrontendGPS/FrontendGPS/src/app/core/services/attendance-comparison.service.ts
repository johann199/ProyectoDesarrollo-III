import { Injectable } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';
import { ApiService } from './api.service';
import { apiConstants } from '../constants/api.constants';
import { AttendanceComparison, AbsentStudent, AttendanceComparisonFilters } from '../interfaces/attendance-comparison.interface';
import { Attendance } from '../interfaces/attendance.interface';
import { Practice } from './practice.service';

@Injectable({
  providedIn: 'root'
})
export class AttendanceComparisonService {
  
  constructor(private apiService: ApiService) {}

  /**
   * Compara las reservas confirmadas con los registros de asistencia
   * para identificar estudiantes que no asistieron
   */
  getAttendanceComparison(filters?: AttendanceComparisonFilters): Observable<AttendanceComparison[]> {
    const params: { [key: string]: string } = {};
    
    // Aplicar filtros de fecha si están disponibles
    if (filters?.startDate) {
      params['startDate'] = filters.startDate;
    }
    if (filters?.endDate) {
      params['endDate'] = filters.endDate;
    }
    if (filters?.laboratoryName) {
      params['laboratoryName'] = filters.laboratoryName;
    }

    // Convertir fechas al formato requerido por el backend (ISO con timezone)
    const attendanceParams: { [key: string]: string } = {};
    
    if (filters?.startDate) {
      attendanceParams['start'] = this.formatDateForBackend(filters.startDate, true); // inicio del día
    }
    if (filters?.endDate) {
      attendanceParams['end'] = this.formatDateForBackend(filters.endDate, false); // fin del día
    }
    if (filters?.laboratoryName) {
      // El endpoint de asistencias no filtra por laboratorio, se hará en frontend
    }

    // Obtener mis prácticas y asistencias del período
    return forkJoin({
      practices: this.apiService.get<any>('/practices/my'), // Retorna objeto paginado
      attendances: this.apiService.get<Attendance[]>(apiConstants.LIST_ATTENDANCES, attendanceParams)
    }).pipe(
      map(({ practices, attendances }) => {
        // Extraer array de prácticas del objeto paginado
        const practicesList: Practice[] = practices.content || [];
        // Filtrar prácticas por fecha y laboratorio en frontend
        const filteredPractices = this.filterPracticesByDateAndLab(practicesList, filters);
        return this.processAttendanceComparison(filteredPractices, attendances, filters);
      })
    );
  }

  /**
   * Obtiene estudiantes que hicieron reservas pero no asistieron en una fecha específica
   */
  getAbsentStudentsByDate(date: string): Observable<AbsentStudent[]> {
    const attendanceParams = {
      start: this.formatDateForBackend(date, true),
      end: this.formatDateForBackend(date, false)
    };
    
    return forkJoin({
      practices: this.apiService.get<any>('/practices/my'), // Retorna objeto paginado
      attendances: this.apiService.get<Attendance[]>(apiConstants.LIST_ATTENDANCES, attendanceParams)
    }).pipe(
      map(({ practices, attendances }) => {
        // Extraer array de prácticas del objeto paginado
        const practicesList: Practice[] = practices.content || [];
        // Filtrar prácticas solo para la fecha específica
        const practicesForDate = practicesList.filter(practice => {
          const practiceDate = new Date(practice.date).toDateString();
          const targetDate = new Date(date).toDateString();
          return practiceDate === targetDate;
        });
        return this.extractAbsentStudents(practicesForDate, attendances);
      })
    );
  }

  /**
   * Exporta reporte de comparación de asistencias
   */
  exportAttendanceComparisonReport(filters?: AttendanceComparisonFilters): Observable<Blob> {
    const params: { [key: string]: string } = {};
    
    if (filters?.startDate) {
      params['startDate'] = filters.startDate;
    }
    if (filters?.endDate) {
      params['endDate'] = filters.endDate;
    }
    if (filters?.laboratoryName) {
      params['laboratoryName'] = filters.laboratoryName;
    }
    if (filters?.minAttendanceRate !== undefined) {
      params['minAttendanceRate'] = filters.minAttendanceRate.toString();
    }

    // Para exportación de blob, necesitaríamos un método específico en ApiService
    // Por ahora retornamos una implementación mock
    return new Observable<Blob>(observer => {
      this.getAttendanceComparison(filters).subscribe({
        next: (comparisons) => {
          const csvContent = this.generateCSVContent(comparisons);
          const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' });
          observer.next(blob);
          observer.complete();
        },
        error: (error) => observer.error(error)
      });
    });
  }

  private processAttendanceComparison(
    practices: Practice[], 
    attendances: Attendance[], 
    filters?: AttendanceComparisonFilters
  ): AttendanceComparison[] {
    
    const comparisons: AttendanceComparison[] = [];

    practices.forEach(practice => {
      // Filtrar asistencias para esta práctica específica
      const practiceAttendances = this.filterAttendancesByPractice(practice, attendances);
      
      // Calcular estudiantes ausentes (esta lógica puede necesitar ajustes según el backend)
      const absentStudents = this.calculateAbsentStudents(practice, practiceAttendances);
      
      // Calcular tasa de asistencia
      const attendanceRate = practice.studentCount > 0 
        ? ((practice.studentCount - absentStudents.length) / practice.studentCount) * 100 
        : 100;

      // Aplicar filtro de tasa mínima de asistencia si está definido
      if (filters?.minAttendanceRate !== undefined && attendanceRate < filters.minAttendanceRate) {
        return; // Saltar esta práctica
      }

      const comparison: AttendanceComparison = {
        practiceId: practice.id,
        practiceDate: practice.date,
        practiceTime: `${practice.startTime} - ${practice.endTime}`,
        subject: practice.subject,
        laboratoryName: practice.laboratoryName,
        expectedStudents: practice.studentCount,
        attendedStudents: practice.studentCount - absentStudents.length,
        absentStudents: absentStudents,
        attendanceRate: Math.round(attendanceRate * 100) / 100 // Redondear a 2 decimales
      };

      comparisons.push(comparison);
    });

    return comparisons.sort((a, b) => new Date(b.practiceDate).getTime() - new Date(a.practiceDate).getTime());
  }

  private filterAttendancesByPractice(practice: Practice, attendances: Attendance[]): Attendance[] {
    const practiceDate = new Date(practice.date);
    const startTime = this.parseTime(practice.startTime);
    const endTime = this.parseTime(practice.endTime);

    return attendances.filter(attendance => {
      const attendanceDate = new Date(attendance.timestamp);
      const attendanceTime = attendanceDate.getHours() * 60 + attendanceDate.getMinutes();
      
      // Verificar si la asistencia está en la fecha correcta
      const sameDate = attendanceDate.toDateString() === practiceDate.toDateString();
      
      // Verificar si la asistencia está dentro del rango de tiempo de la práctica
      const withinTimeRange = attendanceTime >= startTime && attendanceTime <= endTime;

      return sameDate && withinTimeRange;
    });
  }

  private calculateAbsentStudents(practice: Practice, attendances: Attendance[]): AbsentStudent[] {
    // Esta función necesitará ser ajustada según como el backend maneje las reservas
    // Por ahora, asumimos que la diferencia entre estudiantes esperados y asistentes son los ausentes
    
    const absentStudents: AbsentStudent[] = [];
    const expectedCount = practice.studentCount;
    const actualCount = attendances.length;
    
    // Esta es una implementación simplificada
    // En un escenario real, necesitarías los datos específicos de quién se registró para la práctica
    for (let i = actualCount; i < expectedCount; i++) {
      absentStudents.push({
        userCode: `UNKNOWN_${i}`, // Esto necesitaría datos reales del backend
        userName: `Estudiante Ausente ${i + 1}`,
        userEmail: `estudiante${i + 1}@unknown.edu`,
        practiceId: practice.id,
        practiceDate: practice.date,
        reservationTime: `${practice.startTime} - ${practice.endTime}`
      });
    }

    return absentStudents;
  }

  private extractAbsentStudents(practices: Practice[], attendances: Attendance[]): AbsentStudent[] {
    const allAbsent: AbsentStudent[] = [];
    
    practices.forEach(practice => {
      const practiceAttendances = this.filterAttendancesByPractice(practice, attendances);
      const absentForThisPractice = this.calculateAbsentStudents(practice, practiceAttendances);
      allAbsent.push(...absentForThisPractice);
    });

    return allAbsent;
  }

  private parseTime(timeString: string): number {
    const [hours, minutes] = timeString.split(':').map(Number);
    return hours * 60 + minutes;
  }

  private generateCSVContent(comparisons: AttendanceComparison[]): string {
    const headers = [
      'ID Práctica', 'Fecha', 'Horario', 'Asignatura', 'Laboratorio',
      'Estudiantes Esperados', 'Estudiantes Asistentes', 'Estudiantes Ausentes', 
      'Tasa de Asistencia (%)'
    ];

    const rows = comparisons.map(comparison => [
      comparison.practiceId.toString(),
      comparison.practiceDate,
      comparison.practiceTime,
      comparison.subject,
      comparison.laboratoryName,
      comparison.expectedStudents.toString(),
      comparison.attendedStudents.toString(),
      comparison.absentStudents.length.toString(),
      comparison.attendanceRate.toString()
    ]);

    return [headers.join(','), ...rows.map(row => row.join(','))].join('\n');
  }

  /**
   * Formatea fecha para el backend en formato ISO con timezone
   * Ejemplo: 2025-12-01T00:00:00-05:00
   */
  private formatDateForBackend(dateString: string, isStartOfDay: boolean): string {
    const date = new Date(dateString);
    
    if (isStartOfDay) {
      // Inicio del día: 00:00:00
      date.setHours(0, 0, 0, 0);
    } else {
      // Fin del día: 23:59:59
      date.setHours(23, 59, 59, 999);
    }
    
    // Formato ISO con timezone colombiano (UTC-5)
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}-05:00`;
  }

  /**
   * Filtra prácticas por fecha y laboratorio
   */
  private filterPracticesByDateAndLab(practices: Practice[], filters?: AttendanceComparisonFilters): Practice[] {
    return practices.filter(practice => {
      // Filtro por fecha
      if (filters?.startDate || filters?.endDate) {
        const practiceDate = new Date(practice.date);
        
        if (filters.startDate) {
          const startDate = new Date(filters.startDate);
          if (practiceDate < startDate) return false;
        }
        
        if (filters.endDate) {
          const endDate = new Date(filters.endDate);
          if (practiceDate > endDate) return false;
        }
      }
      
      // Filtro por laboratorio
      if (filters?.laboratoryName) {
        const labFilter = filters.laboratoryName.toLowerCase();
        const practiceLab = practice.laboratoryName.toLowerCase();
        if (!practiceLab.includes(labFilter)) return false;
      }
      
      return true;
    });
  }
}