package com.approvalgateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A minimal "sliding window" rate limiter, in memory, no external dependencies.
 *
 * It exists to protect the AI-generation endpoint: Gemini's free tier has a request
 * quota, and without a limit here a few accidental double-clicks (or a bored
 * interviewer) can burn through it. Good enough for a single-instance app; a real
 * multi-instance deployment would need a shared store (e.g. Redis) instead of an
 * in-memory deque, since each instance would otherwise count independently.
 */
@Component
public class SimpleRateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Deque<Instant> timestamps = new ArrayDeque<>();
    private final Object lock = new Object();

    public SimpleRateLimiter(
            @Value("${ai.rate-limit.max-requests:5}") int maxRequests,
            @Value("${ai.rate-limit.window-seconds:60}") long windowSeconds) {
        this.maxRequests = maxRequests;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    /** Returns true and records the call if under the limit; false if the limit is hit. */
    public boolean tryAcquire() {
        synchronized (lock) {
            Instant now = Instant.now();
            while (!timestamps.isEmpty() && Duration.between(timestamps.peekFirst(), now).compareTo(window) > 0) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }
}
