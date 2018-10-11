package ru.nordavind.ecgdonglelib.storage.mitbih;

/**
 * Created by Babay on 22.12.2014.
 */
public enum MitBihFormat {

    /**
     * for internal use only - less math
     */
    Format212unsigned,

    /**
     * use this for exports
     */

    Format212,

    /**
     * 16-bit format; big-endian
     */
    Format61,

    /**
     * 16-bit format; little-endian
     */
    Format16;

    public static MitBihFormat getByName(String name) {
        switch (name) {
            case "212u":
                return Format212unsigned;

            case "212":
                return Format212;

            case "61":
                return Format61;

            case "16":
                return Format16;
        }

        for (MitBihFormat format : values()) {
            if (format.name().equals(name))
                return format;
        }

        return null;
    }

    public String getName() {
        switch (this) {
            case Format212unsigned:
                return "212u";

            case Format212:
                return "212";

            case Format61:
                return "61";

            case Format16:
                return "16";
        }
        throw new IllegalStateException("format not implemented");
    }

    public int getSampleSize() {
        switch (this) {
            case Format16:
            case Format61:
                return 16;

            case Format212:
            case Format212unsigned:
                return 12;

            default:
                throw new UnsupportedOperationException("not implemented for format " + name());
        }
    }
}
