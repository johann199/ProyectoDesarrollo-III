export interface Role {
  id: number;
  name: string;
}

export interface User {
  code: string;
  name: string;
  email: string;
  document: string;
  password: string | null;
  role: Role;
}

export interface Attendance {
  id: number;
  user: User;
  timestamp: string;
}

export interface AttendanceFilters {
  start?: string;
  end?: string;
  userCode?: string;
}