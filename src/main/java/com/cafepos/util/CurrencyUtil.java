package com.cafepos.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Shared currency formatting helpers. */
public final class CurrencyUtil {

    private static final NumberFormat FMT =
        NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyUtil() {}

    /** Format a value as "$1,234.56". */
    public static String format(double amount) {
        return FMT.format(amount);
    }
}
