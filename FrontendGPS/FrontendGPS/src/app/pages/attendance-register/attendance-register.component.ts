import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { ApiService } from '../../core/services/api.service';
import { apiConstants } from '../../core/constants/api.constants';
import { Attendance, AttendanceFilters } from '../../core/interfaces/attendance.interface';
import { AttendanceComparison, AttendanceComparisonFilters, AbsentStudent } from '../../core/interfaces/attendance-comparison.interface';
import { AttendanceComparisonService } from '../../core/services/attendance-comparison.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-attendance-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatMenuModule
  ],
  templateUrl: './attendance-register.component.html',
  styleUrl: './attendance-register.component.scss'
})
export class AttendanceRegisterComponent implements OnInit {
  attendanceForm: FormGroup;
  filterForm: FormGroup;
  monthlyReportForm: FormGroup;
  comparisonForm: FormGroup;
  isLoading = false;
  isLoadingList = false;
  isExporting = false;
  isLoadingComparison = false;
  studentData: any = null;
  errorMessage = '';
  
  // Lista de asistencias
  attendancesList: Attendance[] = [];
  displayedColumns: string[] = ['codigo', 'nombre', 'documento', 'rol', 'fecha'];
  
  // Comparación de asistencias
  comparisonResults: AttendanceComparison[] = [];
  absentStudents: AbsentStudent[] = [];
  comparisonDisplayedColumns: string[] = ['practica', 'fecha', 'horario', 'esperados', 'asistentes', 'ausentes', 'tasa'];
  absentDisplayedColumns: string[] = ['codigo', 'nombre', 'email', 'practica', 'fecha'];

  // Opciones para selector de mes
  months = [
    { value: 1, name: 'Enero' },
    { value: 2, name: 'Febrero' },
    { value: 3, name: 'Marzo' },
    { value: 4, name: 'Abril' },
    { value: 5, name: 'Mayo' },
    { value: 6, name: 'Junio' },
    { value: 7, name: 'Julio' },
    { value: 8, name: 'Agosto' },
    { value: 9, name: 'Septiembre' },
    { value: 10, name: 'Octubre' },
    { value: 11, name: 'Noviembre' },
    { value: 12, name: 'Diciembre' }
  ];

  years = Array.from({length: 5}, (_, i) => new Date().getFullYear() - i);

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    private router: Router,
    private comparisonService: AttendanceComparisonService
  ) {
    this.attendanceForm = this.fb.group({
      studentCode: ['', [Validators.required, Validators.minLength(7), Validators.pattern('^[0-9]+$')]]
    });

    this.filterForm = this.fb.group({
      start: [''],
      end: [''],
      userCode: ['']
    });

    this.monthlyReportForm = this.fb.group({
      month: [new Date().getMonth() + 1, Validators.required],
      year: [new Date().getFullYear(), Validators.required]
    });

    this.comparisonForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      laboratoryName: [''],
      minAttendanceRate: [0, [Validators.min(0), Validators.max(100)]]
    });
  }

  ngOnInit() {
    this.loadAttendances();
  }

  loadAttendances(filters?: AttendanceFilters) {
    this.isLoadingList = true;
    
    const params: { [key: string]: string } = {};
    
    if (filters?.start) {
      params['start'] = new Date(filters.start).toISOString();
    }
    if (filters?.end) {
      params['end'] = new Date(filters.end).toISOString();
    }
    if (filters?.userCode) {
      params['userCode'] = filters.userCode;
    }

    this.apiService.get<Attendance[]>(apiConstants.LIST_ATTENDANCES, params).subscribe({
      next: (response) => {
        this.isLoadingList = false;
        this.attendancesList = response.map(attendance => ({
          ...attendance,
          user: {
            ...attendance.user,
            name: this.decodeSpecialCharacters(attendance.user.name)
          }
        }));
      },
      error: (error) => {
        this.isLoadingList = false;
        console.error('Error cargando asistencias:', error);
        this.showSnackBar('Error al cargar las asistencias', 'error');
      }
    });
  }

  onFilterAttendances() {
    const filters: AttendanceFilters = {
      start: this.filterForm.get('start')?.value,
      end: this.filterForm.get('end')?.value,
      userCode: this.filterForm.get('userCode')?.value
    };
    
    this.loadAttendances(filters);
  }

  clearFilters() {
    this.filterForm.reset();
    this.loadAttendances();
  }

  onSearchStudent() {
    if (this.attendanceForm.get('studentCode')?.invalid) {
      this.attendanceForm.markAllAsTouched();
      return;
    }

    const studentCode = this.attendanceForm.get('studentCode')?.value;
    this.isLoading = true;
    this.studentData = null;
    this.errorMessage = '';

    this.apiService.post(`${apiConstants.ATTENDANCES}/${studentCode}`, {}).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.studentData = response.user; 
        this.errorMessage = '';
        
        if (this.studentData.name) {
          this.studentData.name = this.decodeSpecialCharacters(this.studentData.name);
        }
        
        this.showSnackBar('Asistencia registrada exitosamente', 'success');
        
        // Recargar la lista de asistencias
        this.loadAttendances();
      },
      error: (error) => {
        this.isLoading = false;
        
        if (error.status === 404) {
          this.errorMessage = 'Estudiante no encontrado. Verifique el código.';
        } else if (error.status === 400) {
          this.errorMessage = 'La asistencia ya fue registrada hoy para este estudiante.';
        } else if (error.status === 401) {
          this.errorMessage = 'No autorizado. Token inválido o expirado.';
          this.router.navigate(['/login']);
        } else if (error.status === 403) {
          this.errorMessage = 'No tiene permisos para realizar esta acción.';
        } else if (error.status === 500) {
          this.errorMessage = 'Error interno del servidor. Contacte al administrador.';
        } else {
          this.errorMessage = `Error al registrar la asistencia: ${error.message}`;
        }
        this.showSnackBar(this.errorMessage, 'error');
      }
    });
  }

  resetForm() {
    this.attendanceForm.reset();
    this.studentData = null;
    this.errorMessage = '';
  }

  formatDate(timestamp: string): string {
    return new Date(timestamp).toLocaleString('es-CO', {
      timeZone: 'America/Bogota',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  private showSnackBar(message: string, type: 'success' | 'error') {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      panelClass: type === 'success' ? 'snackbar-success' : 'snackbar-error'
    });
  }

  get currentTime(): string {
    return new Date().toLocaleString('es-CO', {
      timeZone: 'America/Bogota',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  // Métodos auxiliares para estadísticas en template
  getTotalExpectedStudents(): number {
    return this.comparisonResults.reduce((sum, comp) => sum + comp.expectedStudents, 0);
  }

  getTotalAttendedStudents(): number {
    return this.comparisonResults.reduce((sum, comp) => sum + comp.attendedStudents, 0);
  }

  getAverageAttendanceRate(): number {
    if (this.comparisonResults.length === 0) return 0;
    const total = this.comparisonResults.reduce((sum, comp) => sum + comp.attendanceRate, 0);
    return Math.round((total / this.comparisonResults.length) * 100) / 100;
  }

  // Métodos de exportación
  async exportMonthlyReport(format: 'csv' | 'pdf') {
    const month = this.monthlyReportForm.get('month')?.value;
    const year = this.monthlyReportForm.get('year')?.value;

    if (!month || !year) {
      this.showSnackBar('Seleccione el mes y año para el reporte', 'error');
      return;
    }

    this.isExporting = true;

    // Calcular fechas de inicio y fin del mes
    const startDate = new Date(year, month - 1, 1);
    const endDate = new Date(year, month, 0, 23, 59, 59);

    const filters: AttendanceFilters = {
      start: startDate.toISOString(),
      end: endDate.toISOString()
    };

    try {
      // Obtener datos del mes específico
      const params: { [key: string]: string } = {};
      params['start'] = startDate.toISOString();
      params['end'] = endDate.toISOString();

      const response = await this.apiService.get<Attendance[]>(apiConstants.LIST_ATTENDANCES, params).toPromise();
      
      if (!response || response.length === 0) {
        this.showSnackBar('No hay asistencias para el período seleccionado', 'error');
        this.isExporting = false;
        return;
      }

      const attendances = response.map(attendance => ({
        ...attendance,
        user: {
          ...attendance.user,
          name: this.decodeSpecialCharacters(attendance.user.name)
        }
      }));

      if (format === 'csv') {
        this.exportToCSV(attendances, month, year);
      } else {
        this.exportToPDF(attendances, month, year);
      }

      this.showSnackBar(`Reporte ${format.toUpperCase()} generado exitosamente`, 'success');
    } catch (error) {
      console.error('Error al generar reporte:', error);
      this.showSnackBar('Error al generar el reporte', 'error');
    } finally {
      this.isExporting = false;
    }
  }

  private exportToCSV(attendances: Attendance[], month: number, year: number) {
    const monthName = this.months.find(m => m.value === month)?.name || month.toString();
    
    // Crear encabezados
    const headers = ['Código', 'Nombre Completo', 'Documento', 'Rol', 'Fecha y Hora'];
    
    // Convertir datos a formato CSV
    const csvData = attendances.map(attendance => [
      attendance.user.code,
      attendance.user.name,
      attendance.user.document,
      attendance.user.role.name,
      this.formatDate(attendance.timestamp)
    ]);

    // Crear contenido CSV
    const csvContent = [
      `Reporte Mensual de Asistencias - ${monthName} ${year}`,
      `Generado el: ${new Date().toLocaleString('es-CO')}`,
      `Total de registros: ${attendances.length}`,
      '', // Línea vacía
      headers.join(','),
      ...csvData.map(row => row.join(','))
    ].join('\n');

    // Crear y descargar archivo
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' });
    saveAs(blob, `reporte-asistencias-${monthName.toLowerCase()}-${year}.csv`);
  }

  private exportToPDF(attendances: Attendance[], month: number, year: number) {
    const monthName = this.months.find(m => m.value === month)?.name || month.toString();
    
    // Crear documento PDF
    const doc = new jsPDF();
    
    // Configurar fuentes y colores
    doc.setFontSize(20);
    doc.text('Reporte Mensual de Asistencias', 20, 25);
    
    doc.setFontSize(14);
    doc.text(`Período: ${monthName} ${year}`, 20, 35);
    doc.text(`Generado el: ${new Date().toLocaleString('es-CO')}`, 20, 45);
    doc.text(`Total de registros: ${attendances.length}`, 20, 55);

    // Preparar datos para la tabla
    const tableData = attendances.map(attendance => [
      attendance.user.code,
      attendance.user.name,
      attendance.user.document,
      attendance.user.role.name,
      this.formatDate(attendance.timestamp)
    ]);

    // Crear tabla
    autoTable(doc, {
      head: [['Código', 'Nombre Completo', 'Documento', 'Rol', 'Fecha y Hora']],
      body: tableData,
      startY: 65,
      styles: {
        fontSize: 8,
        cellPadding: 3
      },
      headStyles: {
        fillColor: [33, 150, 243],
        textColor: 255,
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245]
      },
      columnStyles: {
        0: { cellWidth: 25 },
        1: { cellWidth: 50 },
        2: { cellWidth: 30 },
        3: { cellWidth: 25 },
        4: { cellWidth: 45 }
      }
    });

    // Agregar pie de página
    const pageCount = doc.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.text(
        `Página ${i} de ${pageCount}`,
        doc.internal.pageSize.width - 30,
        doc.internal.pageSize.height - 10
      );
    }

    // Descargar archivo
    doc.save(`reporte-asistencias-${monthName.toLowerCase()}-${year}.pdf`);
  }

  // Métodos de comparación de asistencias
  async loadAttendanceComparison() {
    if (this.comparisonForm.invalid) {
      this.comparisonForm.markAllAsTouched();
      this.showSnackBar('Complete los campos requeridos para la comparación', 'error');
      return;
    }

    this.isLoadingComparison = true;
    this.comparisonResults = [];
    this.absentStudents = [];

    try {
      const filters: AttendanceComparisonFilters = {
        startDate: this.comparisonForm.get('startDate')?.value,
        endDate: this.comparisonForm.get('endDate')?.value,
        laboratoryName: this.comparisonForm.get('laboratoryName')?.value || undefined,
        minAttendanceRate: this.comparisonForm.get('minAttendanceRate')?.value || undefined
      };

      const comparisons = await this.comparisonService.getAttendanceComparison(filters).toPromise();
      
      if (comparisons) {
        this.comparisonResults = comparisons;
        
        // Extraer todos los estudiantes ausentes
        this.absentStudents = [];
        comparisons.forEach(comparison => {
          this.absentStudents.push(...comparison.absentStudents);
        });

        this.showSnackBar(`Comparación completada: ${comparisons.length} prácticas analizadas`, 'success');
      } else {
        this.showSnackBar('No se encontraron prácticas para el período seleccionado', 'error');
      }
    } catch (error) {
      console.error('Error en comparación de asistencias:', error);
      this.showSnackBar('Error al realizar la comparación de asistencias', 'error');
    } finally {
      this.isLoadingComparison = false;
    }
  }

  clearComparison() {
    this.comparisonForm.reset();
    this.comparisonResults = [];
    this.absentStudents = [];
  }

  exportComparisonReport(format: 'csv' | 'pdf') {
    if (this.comparisonResults.length === 0) {
      this.showSnackBar('No hay datos de comparación para exportar', 'error');
      return;
    }

    this.isExporting = true;

    try {
      if (format === 'csv') {
        this.exportComparisonToCSV();
      } else {
        this.exportComparisonToPDF();
      }
      
      this.showSnackBar(`Reporte de comparación ${format.toUpperCase()} generado exitosamente`, 'success');
    } catch (error) {
      console.error('Error al exportar comparación:', error);
      this.showSnackBar('Error al generar el reporte de comparación', 'error');
    } finally {
      this.isExporting = false;
    }
  }

  private exportComparisonToCSV() {
    // Reporte general de comparación
    const comparisonHeaders = [
      'ID Práctica', 'Fecha', 'Horario', 'Asignatura', 'Laboratorio',
      'Estudiantes Esperados', 'Estudiantes Asistentes', 'Estudiantes Ausentes', 
      'Tasa de Asistencia (%)'
    ];
    
    const comparisonData = this.comparisonResults.map(comp => [
      comp.practiceId.toString(),
      comp.practiceDate,
      comp.practiceTime,
      comp.subject,
      comp.laboratoryName,
      comp.expectedStudents.toString(),
      comp.attendedStudents.toString(),
      comp.absentStudents.length.toString(),
      comp.attendanceRate.toFixed(2)
    ]);

    // Reporte de estudiantes ausentes
    const absentHeaders = [
      'Código Estudiante', 'Nombre', 'Email', 'ID Práctica', 'Fecha Práctica', 'Horario'
    ];
    
    const absentData = this.absentStudents.map(absent => [
      absent.userCode,
      absent.userName,
      absent.userEmail,
      absent.practiceId.toString(),
      absent.practiceDate,
      absent.reservationTime
    ]);

    // Combinar ambos reportes
    const csvContent = [
      'REPORTE DE COMPARACIÓN DE ASISTENCIAS',
      `Generado el: ${new Date().toLocaleString('es-CO')}`,
      `Período: ${this.comparisonForm.get('startDate')?.value} - ${this.comparisonForm.get('endDate')?.value}`,
      '',
      'RESUMEN POR PRÁCTICA',
      comparisonHeaders.join(','),
      ...comparisonData.map(row => row.join(',')),
      '',
      'ESTUDIANTES AUSENTES DETALLADO',
      absentHeaders.join(','),
      ...absentData.map(row => row.join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' });
    const fileName = `comparacion-asistencias-${new Date().toISOString().split('T')[0]}.csv`;
    saveAs(blob, fileName);
  }

  private exportComparisonToPDF() {
    const doc = new jsPDF();
    
    // Encabezado
    doc.setFontSize(18);
    doc.text('Reporte de Comparación de Asistencias', 20, 20);
    
    doc.setFontSize(12);
    doc.text(`Generado el: ${new Date().toLocaleString('es-CO')}`, 20, 30);
    doc.text(`Período: ${this.comparisonForm.get('startDate')?.value} - ${this.comparisonForm.get('endDate')?.value}`, 20, 40);

    // Estadísticas generales
    const totalPractices = this.comparisonResults.length;
    const totalExpectedStudents = this.comparisonResults.reduce((sum, comp) => sum + comp.expectedStudents, 0);
    const totalAttendedStudents = this.comparisonResults.reduce((sum, comp) => sum + comp.attendedStudents, 0);
    const avgAttendanceRate = totalPractices > 0 
      ? (this.comparisonResults.reduce((sum, comp) => sum + comp.attendanceRate, 0) / totalPractices).toFixed(2)
      : '0.00';

    doc.text(`Total de prácticas analizadas: ${totalPractices}`, 20, 50);
    doc.text(`Total de estudiantes esperados: ${totalExpectedStudents}`, 20, 60);
    doc.text(`Total de estudiantes que asistieron: ${totalAttendedStudents}`, 20, 70);
    doc.text(`Tasa promedio de asistencia: ${avgAttendanceRate}%`, 20, 80);

    // Tabla de comparación
    const tableData = this.comparisonResults.map(comp => [
      comp.practiceId.toString(),
      comp.practiceDate,
      comp.subject,
      comp.laboratoryName,
      comp.expectedStudents.toString(),
      comp.attendedStudents.toString(),
      comp.absentStudents.length.toString(),
      `${comp.attendanceRate.toFixed(2)}%`
    ]);

    autoTable(doc, {
      head: [['ID', 'Fecha', 'Asignatura', 'Laboratorio', 'Esperados', 'Asistentes', 'Ausentes', 'Tasa %']],
      body: tableData,
      startY: 90,
      styles: {
        fontSize: 8,
        cellPadding: 2
      },
      headStyles: {
        fillColor: [63, 81, 181],
        textColor: 255,
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245]
      }
    });

    // Tabla de estudiantes ausentes si hay datos
    if (this.absentStudents.length > 0) {
      const finalY = (doc as any).lastAutoTable.finalY + 20;
      
      doc.setFontSize(14);
      doc.text('Estudiantes Ausentes (Detalle)', 20, finalY);

      const absentTableData = this.absentStudents.map(absent => [
        absent.userCode,
        absent.userName,
        absent.userEmail,
        absent.practiceDate,
        absent.reservationTime
      ]);

      autoTable(doc, {
        head: [['Código', 'Nombre', 'Email', 'Fecha', 'Horario']],
        body: absentTableData,
        startY: finalY + 10,
        styles: {
          fontSize: 7,
          cellPadding: 2
        },
        headStyles: {
          fillColor: [244, 67, 54],
          textColor: 255,
          fontStyle: 'bold'
        },
        alternateRowStyles: {
          fillColor: [245, 245, 245]
        }
      });
    }

    const fileName = `comparacion-asistencias-${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);
  }

  private decodeSpecialCharacters(text: string): string {
    if (!text) return '';
    
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