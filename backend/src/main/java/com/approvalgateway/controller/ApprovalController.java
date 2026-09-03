package com.approvalgateway.controller;

import com.approvalgateway.controller.dto.DecisionRequest;
import com.approvalgateway.controller.dto.SimulateEmailRequest;
import com.approvalgateway.model.ApprovalRequest;
import com.approvalgateway.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/automations/email-draft/simulate")
    public ApprovalRequest simulateEmailDraft(@Valid @RequestBody SimulateEmailRequest request) {
        return approvalService.simulateEmailDraftAutomation(request.customerEmail(), request.customerMessage());
    }

    @GetMapping("/approvals")
    public List<ApprovalRequest> list(@RequestParam(required = false) String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return approvalService.findPending();
        }
        return approvalService.findAll();
    }

    @GetMapping("/approvals/{id}")
    public ApprovalRequest get(@PathVariable Long id) {
        return approvalService.findById(id);
    }

    @PostMapping("/approvals/{id}/approve")
    public ApprovalRequest approve(@PathVariable Long id, @RequestBody(required = false) DecisionRequest body) {
        String note = body == null ? null : body.reviewerNote();
        return approvalService.approve(id, note);
    }

    @PostMapping("/approvals/{id}/reject")
    public ApprovalRequest reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest body) {
        String note = body == null ? null : body.reviewerNote();
        return approvalService.reject(id, note);
    }
}
