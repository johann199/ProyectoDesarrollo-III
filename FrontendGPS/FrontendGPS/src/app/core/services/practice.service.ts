import { Observable } from "rxjs/internal/Observable";
import { apiConstants } from "../constants/api.constants";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";

export interface Practice {
  id: number;
  subject: string;
  practiceType: string;
  date: string;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  studentCount: number;
  laboratoryName: string;
}

export interface PracticeRequest {
  subject: string;
  laboratoryName: string;
  practiceType: string;
  date: string;
  startTime: string;
  durationMinutes: number;
  studentCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class PracticeService {
  constructor(private apiService: ApiService) {}

  createPractice(data: PracticeRequest): Observable<any> {
    return this.apiService.post(apiConstants.PRACTICES, data);
  }

  getMyPractices(): Observable<Practice[]> {
    return this.apiService.get('/practices/my');
  }
}
