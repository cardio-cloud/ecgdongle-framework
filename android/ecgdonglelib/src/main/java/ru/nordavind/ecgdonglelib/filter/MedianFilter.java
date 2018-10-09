package ru.nordavind.ecgdonglelib.filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Locale;

/**
 * Contains settings of a median filter
 */

public class MedianFilter implements IFilter, Serializable {
    private static final String ORDER = "o";
    public final int order;

    public MedianFilter(int order) {
        this.order = order;
    }

    public MedianFilter(JSONObject source) throws JSONException {
        order = source.getInt(ORDER);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.Median;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Filter.FILTER_TYPE, FilterType.Median.name());
        jsonObject.put(ORDER, order);
        return jsonObject;
    }

    @Override
    public String describeEn() {
        return String.format(Locale.US, "%s=%d", getFilterType().displayNameEn(), order);
    }
}
