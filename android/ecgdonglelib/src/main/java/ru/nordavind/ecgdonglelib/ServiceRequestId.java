package ru.nordavind.ecgdonglelib;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ids of requests to ECG Dongle Service
 */
@IntDef({
        ServiceRequestId.START,
        ServiceRequestId.GET_DEVICES,
        ServiceRequestId.START_SCANNING,
        ServiceRequestId.SET_FILTERS,
        ServiceRequestId.STOP_SCANNING,
        ServiceRequestId.STOP_LISTEN,
})
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceRequestId {
    int START = 1;
    int GET_DEVICES = 3;
    int START_SCANNING = 4;
    int SET_FILTERS = 5;
    int STOP_SCANNING = 6;
    int STOP_LISTEN = 7;
}
