export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ApprovalRequest {
  id: number;
  automationType: string;
  title: string;
  sensitiveDataPreview: string;
  proposedAction: string;
  status: ApprovalStatus;
  createdAt: string;
  decidedAt: string | null;
  reviewerNote: string | null;
}
