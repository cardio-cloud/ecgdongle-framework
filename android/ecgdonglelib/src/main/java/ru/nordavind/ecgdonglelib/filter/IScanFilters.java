package ru.nordavind.ecgdonglelib.filter;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Filters for a scan
 */

public interface IScanFilters {
    /**
     * describes filter in English
     *
     * @return filter description as string
     */
    String describeEn();

    /**
     * @return true if filtering enabled
     */
    boolean isFilteringEnabled();

    /**
     * @return filters list
     */
    @NonNull
    List<IFilter> getFilters();
}
