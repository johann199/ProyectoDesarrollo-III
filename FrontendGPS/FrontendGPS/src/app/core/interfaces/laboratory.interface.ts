export interface Laboratory {
  id: number;
  name: string;
  capacity: number;
  active: boolean;
}

export interface LaboratoryRequest {
  name: string;
  capacity: number;
}

export interface LaboratoryFilters {
  page?: number;
  size?: number;
  sort?: string;
}