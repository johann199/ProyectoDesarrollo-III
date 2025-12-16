export enum UserRole {
  DOCENTE = 'DOCENTE',
  ESTUDIANTE = 'ESTUDIANTE',
  MONITOR = 'MONITOR',
  ADMIN = 'ADMIN'
}

export const AVAILABLE_ROLES = [
  { name: UserRole.DOCENTE },
  { name: UserRole.ESTUDIANTE },
  { name: UserRole.MONITOR },
  { name: UserRole.ADMIN }
];