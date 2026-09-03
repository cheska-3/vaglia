package com.approvalgateway.util;

/**
 * Masks sensitive values before they are persisted or shown in a list view.
 * The full value is only ever used in-memory to build the AI prompt, never stored.
 */
public final class SensitiveDataMasker {

    private SensitiveDataMasker() {
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        String maskedLocal = local.length() <= 2
                ? local.charAt(0) + "*"
                : local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1);
        String maskedDomain = domain.length() <= 2
                ? "*".repeat(domain.length())
                : domain.charAt(0) + "*".repeat(Math.max(1, domain.length() - 2)) + domain.charAt(domain.length() - 1);
        return maskedLocal + "@" + maskedDomain;
    }
}
