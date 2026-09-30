package com.challenge.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Set;

/**
 * Conversions between decimal amounts at the API boundary and the minor units
 * (cents, yen, ...) we store. Always go through here: currencies differ in how
 * many decimal places they have (USD 2, JPY 0).
 */
public final class Money {

    public static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "JPY");

    private Money() {}

    public static void requireSupported(String currency) {
        if (currency == null || !SUPPORTED_CURRENCIES.contains(currency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
    }

    /**
     * Converts a decimal amount to minor units. Rejects amounts with more precision
     * than the currency allows (e.g. 10.005 USD) rather than silently rounding.
     */
    public static long toMinorUnits(BigDecimal amount, String currency) {
        requireSupported(currency);
        int digits = fractionDigits(currency);
        try {
            return amount.setScale(digits, RoundingMode.UNNECESSARY).movePointRight(digits).longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                "Amount " + amount.toPlainString() + " is not valid for " + currency);
        }
    }

    public static BigDecimal toMajorUnits(long minorUnits, String currency) {
        return BigDecimal.valueOf(minorUnits, fractionDigits(currency));
    }

    private static int fractionDigits(String currency) {
        return Currency.getInstance(currency).getDefaultFractionDigits();
    }
}
