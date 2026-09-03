package com.approvalgateway.repository;

import com.approvalgateway.model.ApprovalRequest;
import com.approvalgateway.model.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
    List<ApprovalRequest> findAllByOrderByCreatedAtDesc();
}
