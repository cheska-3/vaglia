import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
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

  runDemoAutomation(): void {
    this.approvalService
      .simulateEmailDraft(this.simCustomerEmail, this.simCustomerMessage)
      .subscribe(() => this.refresh());
  }
}
