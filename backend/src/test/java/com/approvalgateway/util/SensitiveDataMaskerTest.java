package com.approvalgateway.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {

    @Test
    void masksEmailKeepingFirstAndLastCharacterOfEachPart() {
        String masked = SensitiveDataMasker.maskEmail("mario.rossi@cliente.it");

        assertThat(masked).startsWith("m").endsWith("t").contains("@");
        assertThat(masked).doesNotContain("mario.rossi");
        assertThat(masked).doesNotContain("cliente.it");
    }

    @Test
    void masksVeryShortLocalPartWithoutIndexOutOfBounds() {
        String masked = SensitiveDataMasker.maskEmail("ab@cliente.it");

        assertThat(masked).startsWith("a*");
    }

    @Test
    void returnsPlaceholderForInvalidEmail() {
        assertThat(SensitiveDataMasker.maskEmail("not-an-email")).isEqualTo("***");
        assertThat(SensitiveDataMasker.maskEmail(null)).isEqualTo("***");
    }
}
