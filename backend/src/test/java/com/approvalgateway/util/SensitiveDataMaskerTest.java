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

    @Test
    void masksIbanKeepingFirstAndLastFourCharacters() {
        String masked = SensitiveDataMasker.maskIban("IT60X0542811101000000123456");

        assertThat(masked).startsWith("IT60").endsWith("3456");
        assertThat(masked).doesNotContain("0542811101000000");
    }

    @Test
    void masksShortIbanEntirely() {
        assertThat(SensitiveDataMasker.maskIban("IT60X05")).isEqualTo("*******");
    }
}
