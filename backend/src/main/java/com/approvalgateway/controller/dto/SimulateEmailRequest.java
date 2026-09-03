package com.approvalgateway.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SimulateEmailRequest(
        @NotBlank String customerEmail,
        @NotBlank String customerMessage
) {
}
