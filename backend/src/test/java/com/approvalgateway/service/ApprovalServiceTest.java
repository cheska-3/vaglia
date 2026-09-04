package com.approvalgateway.service;

import com.approvalgateway.model.ApprovalRequest;
import com.approvalgateway.model.ApprovalStatus;
import com.approvalgateway.repository.ApprovalRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ApprovalService. The repository and the AI drafting service are
 * mocked: we are testing ApprovalService's own logic in isolation, not JPA or the
 * real Gemini API call (those belong in their own tests).
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRequestRepository repository;

    @Mock
    private AiDraftingService aiDraftingService;

    @Mock
    private SimpleRateLimiter rateLimiter;

    @InjectMocks
    private ApprovalService approvalService;

    @Test
    void simulateEmailDraftAutomation_masksEmailAndSavesPendingRequest() {
        when(rateLimiter.tryAcquire()).thenReturn(true);
        when(aiDraftingService.draftEmailReply(anyString())).thenReturn("bozza generata");
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = approvalService.simulateEmailDraftAutomation(
                "mario.rossi@cliente.it", "quando arriva il mio ordine?");

        ArgumentCaptor<ApprovalRequest> captor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(repository).save(captor.capture());
        ApprovalRequest saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(saved.getProposedAction()).isEqualTo("bozza generata");
        assertThat(saved.getSensitiveDataPreview()).doesNotContain("mario.rossi@cliente.it");
        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    void approve_setsStatusApprovedAndDecidedAt() {
        ApprovalRequest existing = ApprovalRequest.builder()
                .id(1L)
                .status(ApprovalStatus.PENDING)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = approvalService.approve(1L, "va bene così");

        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(result.getDecidedAt()).isNotNull();
        assertThat(result.getReviewerNote()).isEqualTo("va bene così");
    }

    @Test
    void reject_setsStatusRejected() {
        ApprovalRequest existing = ApprovalRequest.builder()
                .id(2L)
                .status(ApprovalStatus.PENDING)
                .build();
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = approvalService.reject(2L, "dati non corretti");

        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(result.getReviewerNote()).isEqualTo("dati non corretti");
    }

    @Test
    void findById_throwsWhenRequestDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvalService.findById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    void simulateEmailDraftAutomation_throwsWhenRateLimitExceeded() {
        when(rateLimiter.tryAcquire()).thenReturn(false);

        assertThatThrownBy(() -> approvalService.simulateEmailDraftAutomation("a@b.it", "msg"))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
