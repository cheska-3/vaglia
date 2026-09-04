package com.approvalgateway.service;

import com.approvalgateway.model.ApprovalRequest;
import com.approvalgateway.model.ApprovalStatus;
import com.approvalgateway.repository.ApprovalRequestRepository;
import com.approvalgateway.util.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRequestRepository repository;
    private final AiDraftingService aiDraftingService;
    private final SimpleRateLimiter rateLimiter;

    /** Simulates an inbound automation event: an AI draft is generated and held for approval. */
    public ApprovalRequest simulateEmailDraftAutomation(String customerEmail, String customerMessage) {
        checkRateLimit();
        String draft = aiDraftingService.draftEmailReply(customerMessage);
        return saveNewRequest("EMAIL_DRAFT", "Bozza risposta cliente",
                SensitiveDataMasker.maskEmail(customerEmail), draft);
    }

    /** Simulates a second, independent automation: a payment ready to be confirmed. */
    public ApprovalRequest simulatePaymentConfirmationAutomation(String payeeIban, double amount, String reason) {
        checkRateLimit();
        String note = aiDraftingService.draftPaymentJustification(amount, reason);
        return saveNewRequest("PAYMENT_CONFIRMATION", "Conferma pagamento",
                SensitiveDataMasker.maskIban(payeeIban), note);
    }

    private void checkRateLimit() {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException("Troppe richieste di generazione AI, riprova tra poco.");
        }
    }

    private ApprovalRequest saveNewRequest(String automationType, String title, String sensitiveDataPreview,
                                            String proposedAction) {
        ApprovalRequest request = ApprovalRequest.builder()
                .automationType(automationType)
                .title(title)
                .sensitiveDataPreview(sensitiveDataPreview)
                .proposedAction(proposedAction)
                .status(ApprovalStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        return repository.save(request);
    }

    public List<ApprovalRequest> findPending() {
        return repository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING);
    }

    public List<ApprovalRequest> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public ApprovalRequest findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Approval request " + id + " not found"));
    }

    public ApprovalRequest approve(Long id, String reviewerNote) {
        ApprovalRequest request = findById(id);
        request.setStatus(ApprovalStatus.APPROVED);
        request.setDecidedAt(Instant.now());
        request.setReviewerNote(reviewerNote);
        // In a real automation this is where the reviewed action would actually be
        // executed (send the email, run the payment, ...). Left as a no-op here:
        // the point of this project is the approval gate, not the downstream integration.
        return repository.save(request);
    }

    public ApprovalRequest reject(Long id, String reviewerNote) {
        ApprovalRequest request = findById(id);
        request.setStatus(ApprovalStatus.REJECTED);
        request.setDecidedAt(Instant.now());
        request.setReviewerNote(reviewerNote);
        return repository.save(request);
    }
}
