package com.approvalgateway.controller;

import com.approvalgateway.model.ApprovalRequest;
import com.approvalgateway.model.ApprovalStatus;
import com.approvalgateway.service.ApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Slice" test: only the web layer is loaded (no real database, no real service
 * logic) — ApprovalService is replaced with a Mockito mock. This checks that the
 * HTTP endpoints are wired correctly (routes, status codes, JSON shape), not the
 * business logic itself (that's ApprovalServiceTest's job).
 */
@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprovalService approvalService;

    @Test
    void listPending_returnsJsonArray() throws Exception {
        ApprovalRequest request = ApprovalRequest.builder()
                .id(1L)
                .automationType("EMAIL_DRAFT")
                .title("Bozza risposta cliente")
                .sensitiveDataPreview("m***o@c***t")
                .proposedAction("bozza")
                .status(ApprovalStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        when(approvalService.findPending()).thenReturn(List.of(request));

        mockMvc.perform(get("/api/approvals").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void approve_returnsUpdatedRequest() throws Exception {
        ApprovalRequest approved = ApprovalRequest.builder()
                .id(1L)
                .status(ApprovalStatus.APPROVED)
                .build();
        when(approvalService.approve(eq(1L), any())).thenReturn(approved);

        mockMvc.perform(post("/api/approvals/1/approve")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
