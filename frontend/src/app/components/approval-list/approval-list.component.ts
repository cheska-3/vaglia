import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApprovalService } from '../../services/approval.service';
import { ApprovalRequest } from '../../models/approval-request.model';

@Component({
  selector: 'app-approval-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './approval-list.component.html',
  styleUrl: './approval-list.component.css'
})
export class ApprovalListComponent implements OnInit {
  requests: ApprovalRequest[] = [];
  showAll = false;
  loading = false;

  simCustomerEmail = 'giulia.bianchi@clienteesempio.it';
  simCustomerMessage = 'Salve, il pagamento della fattura 8832 risulta ancora in sospeso, potete verificare?';
  generatingEmail = false;
  emailError = '';

  simPayeeIban = 'IT60X0542811101000000123456';
  simAmount = 420;
  simReason = 'Pagamento fornitore — fattura 8832';
  generatingPayment = false;
  paymentError = '';

  constructor(private approvalService: ApprovalService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    const source$ = this.showAll ? this.approvalService.listAll() : this.approvalService.listPending();
    source$.subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  toggleShowAll(): void {
    this.showAll = !this.showAll;
    this.refresh();
  }

  runEmailDemoAutomation(): void {
    this.generatingEmail = true;
    this.emailError = '';
    this.approvalService.simulateEmailDraft(this.simCustomerEmail, this.simCustomerMessage).subscribe({
      next: () => {
        this.generatingEmail = false;
        this.refresh();
      },
      error: (err: HttpErrorResponse) => {
        this.generatingEmail = false;
        this.emailError = this.describeError(err);
      }
    });
  }

  runPaymentDemoAutomation(): void {
    this.generatingPayment = true;
    this.paymentError = '';
    this.approvalService.simulatePaymentConfirmation(this.simPayeeIban, this.simAmount, this.simReason).subscribe({
      next: () => {
        this.generatingPayment = false;
        this.refresh();
      },
      error: (err: HttpErrorResponse) => {
        this.generatingPayment = false;
        this.paymentError = this.describeError(err);
      }
    });
  }

  private describeError(err: HttpErrorResponse): string {
    if (err.status === 429) {
      return 'Hai raggiunto il limite di richieste AI per questo minuto. Riprova tra poco.';
    }
    return 'Generazione fallita. Riprova (se hai configurato GEMINI_API_KEY, potresti aver raggiunto il rate limit del tier gratuito).';
  }
}
