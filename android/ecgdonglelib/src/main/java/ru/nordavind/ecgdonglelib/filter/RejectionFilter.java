package ru.nordavind.ecgdonglelib.filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Locale;

import ru.nordavind.ecgdonglelib.util.MathUtil;

/**
 * Contains settings of a Rejection filter
 */

public class RejectionFilter extends Filter implements Serializable {
    private static final String REJECTOR_QUALITY = "q";
    private static final String REJECTOR_Q = "rq";
    private static final String REJECTOR_F = "rf";

    /**
     * rejection filter quality. should be 24 - 100
     */
    public final int rejQuality;

    public final int rej_f;

    public final int rej_q;

    public RejectionFilter(double frequency, int rejQuality, int rej_f, int rej_q) {
        super(FilterType.Rejection, frequency);
        this.rejQuality = rejQuality;
        this.rej_f = rej_f;
        this.rej_q = rej_q;
    }

    public RejectionFilter(JSONObject source) throws JSONException {
        super(source);
        rejQuality = source.getInt(REJECTOR_QUALITY);
        rej_q = source.getInt(REJECTOR_Q);
        rej_f = source.getInt(REJECTOR_F);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject res = super.toJson();
        res.put(REJECTOR_QUALITY, rejQuality);
        res.put(REJECTOR_Q, rej_q);
        res.put(REJECTOR_F, rej_f);
        return res;
    }

    @Override
    public String describeEn() {
        if (rejQuality == 0)
            return super.describeEn();
        return String.format(Locale.US, "%s=%sHz (q=%d)", filterType.displayNameEn(), MathUtil.formatDoubleValue(frequency, 2), rejQuality);
    }
}
