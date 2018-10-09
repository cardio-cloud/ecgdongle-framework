package ru.nordavind.ecgdonglelib.util;

import java.util.Locale;

/**
 * util
 */
public class MathUtil {

    public static String formatDoubleValue(double value, int maxDecimalDigits) {
        if (value == Math.round(value))
            return String.format(Locale.US, "%.0f", value);

        for (int i = 0; i < maxDecimalDigits; i++) {
            double exp = Math.pow(10, i);
            double val = value * exp;
            if (Math.abs(val - Math.round(val)) < 0.001) {
                String formatStr = String.format(Locale.US, "%%.%df", i);
                val = Math.round(val) / exp;
                return String.format(Locale.US, formatStr, val);
            }
        }
        double exp = Math.pow(10, maxDecimalDigits);
        double val = value * exp;
        String formatStr = String.format(Locale.US, "%%.%df", maxDecimalDigits);
        val = Math.round(val) / exp;
        return String.format(Locale.US, formatStr, val);
    }
}
