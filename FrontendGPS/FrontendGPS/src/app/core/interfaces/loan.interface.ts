export interface LoanRequest {
  barcode: string;
  studentCode: string;
  monitorCode: string;
}

export interface LoanResponse {
  id: number;
  equipmentName: string;
  barcode: string;
  studentName: string;
  monitorName: string;
  loanDateTime: string;
  returnDateTime?: string;
  status: 'ACTIVE' | 'RETURNED';
}