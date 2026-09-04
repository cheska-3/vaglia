package com.approvalgateway.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SimulatePaymentRequest(
        @NotBlank String payeeIban,
        @Positive double amount,
        @NotBlank String reason
) {
}
