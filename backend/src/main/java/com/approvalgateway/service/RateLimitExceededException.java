package com.approvalgateway.service;

/** Thrown when too many AI-generation requests arrive in the configured time window. */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
