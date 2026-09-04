import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApprovalRequest } from '../models/approval-request.model';

@Injectable({ providedIn: 'root' })
export class ApprovalService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  listPending(): Observable<ApprovalRequest[]> {
    return this.http.get<ApprovalRequest[]>(`${this.baseUrl}/approvals?status=PENDING`);
  }

  listAll(): Observable<ApprovalRequest[]> {
    return this.http.get<ApprovalRequest[]>(`${this.baseUrl}/approvals`);
  }

  getById(id: number): Observable<ApprovalRequest> {
    return this.http.get<ApprovalRequest>(`${this.baseUrl}/approvals/${id}`);
  }

  approve(id: number, reviewerNote: string): Observable<ApprovalRequest> {
    return this.http.post<ApprovalRequest>(`${this.baseUrl}/approvals/${id}/approve`, { reviewerNote });
  }

  reject(id: number, reviewerNote: string): Observable<ApprovalRequest> {
    return this.http.post<ApprovalRequest>(`${this.baseUrl}/approvals/${id}/reject`, { reviewerNote });
  }

  simulateEmailDraft(customerEmail: string, customerMessage: string): Observable<ApprovalRequest> {
    return this.http.post<ApprovalRequest>(`${this.baseUrl}/automations/email-draft/simulate`, {
      customerEmail,
      customerMessage
    });
  }

  simulatePaymentConfirmation(payeeIban: string, amount: number, reason: string): Observable<ApprovalRequest> {
    return this.http.post<ApprovalRequest>(`${this.baseUrl}/automations/payment-confirmation/simulate`, {
      payeeIban,
      amount,
      reason
    });
  }
}
