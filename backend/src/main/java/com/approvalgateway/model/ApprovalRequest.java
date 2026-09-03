package com.approvalgateway.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * An action an AI automation wants to take on sensitive data, held for
 * human approval before it is actually executed.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String automationType;

    @Column(nullable = false)
    private String title;

    /** Sensitive source data (e.g. an email address), already masked before storage. */
    @Column(nullable = false)
    private String sensitiveDataPreview;

    /** The AI-drafted action awaiting review (e.g. the reply text). */
    @Lob
    @Column(nullable = false, length = 4000)
    private String proposedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant decidedAt;

    private String reviewerNote;
}
