package ru.nordavind.ecgdonglelib.filter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.nordavind.ecgdonglelib.util.MathUtil;


/**
 * Represents Hi-pass and Low-pass filters
 * {@link #frequency} field holds filter frequency in Hz
 *
 */

public class Filter implements IFilter, Serializable {
    public static final String FILTER_TYPE = "t";
    private static final String FREQUENCY = "f";

    /**
     * filter frequency in Hz
     */
    public final double frequency;

    /**
     * filter type
     */
    public final FilterType filterType;

    public Filter(FilterType filterType, double frequency) {
        this.filterType = filterType;
        this.frequency = frequency;
    }

    public Filter(JSONObject source) throws JSONException {
        filterType = FilterType.valueOf(source.getString(FILTER_TYPE));
        frequency = source.getDouble(FREQUENCY);
    }

    public static IFilter parse(JSONObject source) throws JSONException {
        FilterType filterType = FilterType.valueOf(source.getString(Filter.FILTER_TYPE));
        switch (filterType) {
            case Hpf:
            case Lpf:
                return new Filter(source);

            case Rejection:
                return new RejectionFilter(source);

            case Median:
                return new MedianFilter(source);

            default:
                throw new UnsupportedOperationException("not implemented for filter type: " + filterType.name());
        }
    }

    public static List<IFilter> parse(JSONArray source) throws JSONException {
        List<IFilter> result = new ArrayList<>(source.length());
        for (int i = 0; i < source.length(); i++) {
            result.add(parse(source.getJSONObject(i)));
        }
        return result;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FILTER_TYPE, filterType.name());
        jsonObject.put(FREQUENCY, frequency);
        return jsonObject;
    }

    @Override
    public String describeEn() {
        return String.format(Locale.US, "%s=%sHz", filterType.displayNameEn(), MathUtil.formatDoubleValue(frequency, 2));
    }
}