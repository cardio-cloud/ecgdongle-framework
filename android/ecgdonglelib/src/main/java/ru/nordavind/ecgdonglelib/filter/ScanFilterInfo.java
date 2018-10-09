package ru.nordavind.ecgdonglelib.filter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds information of scan filters
 */

public class ScanFilterInfo implements IScanFilters, Serializable {
    private static final String KEY_FILTER_SETTINGS = "s";
    private static final String KEY_FILTERS = "f";

    public final FilterSettings filterSettings;

    public final boolean enabled;

    @NonNull
    private final List<IFilter> filters;

    public ScanFilterInfo(boolean enabled, List<IFilter> filters, FilterSettings filterSettings) {
        this.filterSettings = filterSettings;
        this.enabled = enabled;
        this.filters = filters;
    }

    public ScanFilterInfo(JSONObject source) throws JSONException {
        if (!source.isNull(KEY_FILTER_SETTINGS)) {
            filterSettings = new FilterSettings(source.getJSONObject(KEY_FILTER_SETTINGS));
        } else {
            filterSettings = null;
        }
        enabled = source.has(KEY_FILTERS);
        if (enabled) {
            filters = Filter.parse(source.getJSONArray(KEY_FILTERS));
        } else
            filters = new ArrayList<>();
    }

    @Override
    public boolean isFilteringEnabled() {
        return enabled;
    }

    @Override
    public String describeEn() {
        if (enabled) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < filters.size(); i++) {
                IFilter filter = filters.get(i);
                if (filter.getFilterType().shouldPrint()) {
                    builder.append(filter.describeEn());
                    builder.append(", ");
                }
            }
            if (builder.length() > 2) {
                builder.replace(builder.length() - 2, builder.length(), "");
            }
            return builder.toString();
        } else return "";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        if (filterSettings != null) {
            res.put(KEY_FILTER_SETTINGS, filterSettings.toJson());
        }
        if (enabled) {
            JSONArray jsa = new JSONArray();
            for (IFilter filter : filters) {
                jsa.put(filter.toJson());
            }
            res.put(KEY_FILTERS, jsa);
        }
        return res;
    }

    @Override
    @NonNull
    public List<IFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }
}
