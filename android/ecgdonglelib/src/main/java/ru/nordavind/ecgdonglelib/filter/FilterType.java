package ru.nordavind.ecgdonglelib.filter;

/**
 * Filter types
 */

public enum FilterType {
    /**
     * Low-pass filter
     */
    Lpf,

    /**
     * High-pass filter
     */
    Hpf,

    /**
     * Rejector filter
     */
    Rejector,

    /**
     * Median filter
     */
    Median;

    /**
     * @return true if this filter type is included in FilterINfo description
     */
    public boolean shouldPrint() {
        switch (this) {
            case Median:
                return false;

            default:
                return true;
        }
    }

    /**
     * @return display name of filter type in English
     */
    public String displayNameEn() {
        switch (this) {
            case Hpf:
                return "HPF";
            case Lpf:
                return "LPF";

            case Rejector:
                return "RJ";

            case Median:
                return "Med";

            default:
                throw new RuntimeException("not implemented for " + name());
        }
    }
}
