import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApprovalService } from '../../services/approval.service';
import { ApprovalRequest } from '../../models/approval-request.model';

@Component({
  selector: 'app-approval-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './approval-detail.component.html',
  styleUrl: './approval-detail.component.css'
})
export class ApprovalDetailComponent implements OnInit {
  request: ApprovalRequest | null = null;
  reviewerNote = '';
  submitting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private approvalService: ApprovalService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.approvalService.getById(id).subscribe((r) => (this.request = r));
  }

  approve(): void {
    if (!this.request) return;
    this.submitting = true;
    this.approvalService.approve(this.request.id, this.reviewerNote).subscribe(() => {
      this.router.navigate(['/']);
    });
  }

  reject(): void {
    if (!this.request) return;
    this.submitting = true;
    this.approvalService.reject(this.request.id, this.reviewerNote).subscribe(() => {
      this.router.navigate(['/']);
    });
  }
}
