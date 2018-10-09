package ru.nordavind.ecgdonglelib.scan;

/**
 * Power frequency (50 Hz and 60 Hz). used for rejector filter
 */
public enum PowerFrequencyLib {
    hz50, hz60, None;

    public static PowerFrequencyLib fromInt(int val) {
        switch (val) {
            case 0:
                return hz50;
            case 1:
                return hz60;

            case 2:
                return None;
        }
        return null;
    }

    public static PowerFrequencyLib ofFrequencyHz(int val) {
        switch (val) {
            case 50:
                return hz50;
            case 60:
                return hz60;
        }
        return None;
    }

    public int toInt() {
        switch (this) {
            case hz50:
                return 0;
            case hz60:
                return 1;
            case None:
                return 2;
        }
        throw new UnsupportedOperationException();
    }

    public int getIntFrequency() {
        switch (this) {
            case hz50:
                return 50;
            case hz60:
                return 60;

            case None:
                return 0;
        }
        throw new UnsupportedOperationException();
    }
}
