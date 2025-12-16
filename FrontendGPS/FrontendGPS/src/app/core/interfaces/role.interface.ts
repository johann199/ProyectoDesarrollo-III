import { UserRole } from '../constants/roles.constants';

export interface Role {
  id: number;
  name: UserRole;
}

export interface User {
  code: string;
  name: string;
  email: string;
  document: string;
  password: string | null;
  role: Role;
}