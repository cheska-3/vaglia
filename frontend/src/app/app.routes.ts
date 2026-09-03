import { Routes } from '@angular/router';
import { ApprovalListComponent } from './components/approval-list/approval-list.component';
import { ApprovalDetailComponent } from './components/approval-detail/approval-detail.component';

export const routes: Routes = [
  { path: '', component: ApprovalListComponent },
  { path: 'approvals/:id', component: ApprovalDetailComponent }
];
