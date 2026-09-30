package com.challenge.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void convertsUsingCurrencyFractionDigits() {
        assertThat(Money.toMinorUnits(new BigDecimal("12.34"), "USD")).isEqualTo(1234);
        assertThat(Money.toMinorUnits(new BigDecimal("12"), "USD")).isEqualTo(1200);
        assertThat(Money.toMinorUnits(new BigDecimal("500"), "JPY")).isEqualTo(500);
    }

    @Test
    void rejectsExcessPrecision() {
        assertThatThrownBy(() -> Money.toMinorUnits(new BigDecimal("10.005"), "USD"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Money.toMinorUnits(new BigDecimal("1.5"), "JPY"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnsupportedCurrency() {
        assertThatThrownBy(() -> Money.toMinorUnits(BigDecimal.ONE, "XYZ"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatsMajorUnits() {
        assertThat(Money.toMajorUnits(1234, "USD")).isEqualByComparingTo("12.34");
        assertThat(Money.toMajorUnits(500, "JPY")).isEqualByComparingTo("500");
    }
}
