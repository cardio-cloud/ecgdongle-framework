package ru.nordavind.ecgdonglelib.filter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information of a single filter
 */

public interface IFilter {

    /**
     * @return short description of filter in English
     */
    String describeEn();

    FilterType getFilterType();

    JSONObject toJson() throws JSONException;

}
