export interface AttendanceComparison {
  practiceId: number;
  practiceDate: string;
  practiceTime: string;
  subject: string;
  laboratoryName: string;
  expectedStudents: number;
  attendedStudents: number;
  absentStudents: AbsentStudent[];
  attendanceRate: number;
}

export interface AbsentStudent {
  userCode: string;
  userName: string;
  userEmail: string;
  practiceId: number;
  practiceDate: string;
  reservationTime: string;
}

export interface AttendanceComparisonFilters {
  startDate?: string;
  endDate?: string;
  laboratoryName?: string;
  minAttendanceRate?: number;
}