package ru.nordavind.ecgdonglelib.filter;

import android.support.annotation.IntRange;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import ru.nordavind.ecgdonglelib.Settings;
import ru.nordavind.ecgdonglelib.scan.PowerFrequency;

/**
 * Class used to configure filters
 */

public class FilterSettings implements Serializable {
    /**
     * true if filters should be enabled
     */
    public final boolean doFilter;

    /**
     * Rejection filter quality
     * min = 24, max = 100
     */
    @IntRange(from = Settings.V2_REJECTION_FILTER_MIN_QUALITY, to = Settings.V2_REJECTION_FILTER_MAX_QUALITY)
    public final int rejFilterQ;

    /**
     * Upper frequency for low-pass filter
     */
    public final UpperFrequency upperFrequency;

    /**
     * Frequency for a Rejection filter
     * Rejection filter is used to filter power lines induction noise;
     * 50 Hz ot 60 Hz
     */
    public final PowerFrequency rejectionFilterFrequency;

    private final String KEY_FILTER_ENABLED = "f";
    private final String KEY_REJECT_FILTER_QUALITY = "q";
    private final String KEY_UPPER_FERQUENCY = "u";
    private final String KEY_REJECTION_FILTER_FREQUENCY = "r";

    public FilterSettings(JSONObject source) throws JSONException {
        doFilter = source.getBoolean(KEY_FILTER_ENABLED);
        if (doFilter) {
            rejFilterQ = source.getInt(KEY_REJECT_FILTER_QUALITY);
            rejectionFilterFrequency = PowerFrequency.ofFrequencyHz(source.getInt(KEY_REJECTION_FILTER_FREQUENCY));
            upperFrequency = UpperFrequency.ofFrequency(source.getInt(KEY_UPPER_FERQUENCY));
        } else {
            rejFilterQ = Settings.V2_REJECTION_FILTER_DEF_QUALITY;
            upperFrequency = UpperFrequency.Hz100;
            rejectionFilterFrequency = PowerFrequency.None;
        }
    }

    /**
     * primary used constructor
     *
     * @param doFilter                 set to true to enable filters
     * @param upperFrequency           upper frequency for Low Pass filter
     * @param rejectionFilterFrequency Band-rejection filter is used to remove power line pickup.
     *                                 Set band-rejection filter to 50 or 60 Hz according to
     *                                 powerline frequency used in area where the scan takes place.
     * @param rejFilterQ               band-rejection filter quality. Between 24 and 100.
     */
    public FilterSettings(boolean doFilter, UpperFrequency upperFrequency, PowerFrequency rejectionFilterFrequency, int rejFilterQ) {
        this.doFilter = doFilter;
        this.rejFilterQ = rejFilterQ;
        this.upperFrequency = upperFrequency;
        this.rejectionFilterFrequency = rejectionFilterFrequency;
    }

    public FilterSettings(PowerFrequency rejectionFilterFrequency, FilterSettings source) {
        this.rejectionFilterFrequency = rejectionFilterFrequency;
        this.doFilter = source.doFilter;
        this.rejFilterQ = source.rejFilterQ;
        this.upperFrequency = source.upperFrequency;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(KEY_FILTER_ENABLED, doFilter);
        if (doFilter) {
            result.put(KEY_REJECT_FILTER_QUALITY, rejFilterQ);
            result.put(KEY_REJECTION_FILTER_FREQUENCY, rejectionFilterFrequency.getIntFrequency());
            result.put(KEY_UPPER_FERQUENCY, upperFrequency.frequency);
        }
        return result;
    }

    public enum UpperFrequency {
        Hz35(35), Hz100(100);
        public final int frequency;

        UpperFrequency(int frequency) {
            this.frequency = frequency;
        }

        public static UpperFrequency ofFrequency(int frequency) {
            switch (frequency) {
                case 35:
                    return Hz35;

                case 100:
                    return Hz100;

                default:
                    return Hz100;
            }
        }
    }
}
