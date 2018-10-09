package ru.nordavind.ecgdonglelib;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ids of ECG Dongle Service replies
 */
@IntDef({
        ServiceReplyId.CONNECT_REPLY,
        ServiceReplyId.GET_DEVICES_REPLY,
        ServiceReplyId.ON_DEVICE_CONNECTED,
        ServiceReplyId.ON_DEVICE_DISCONNECTED,
        ServiceReplyId.ON_SCAN_START,
        ServiceReplyId.ON_SCAN_STOP,
        ServiceReplyId.ON_SCAN_START_FAILED,
        ServiceReplyId.ON_SCAN_FILTERS_UPDATED,
})
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceReplyId {
    int CONNECT_REPLY = 1;
    int GET_DEVICES_REPLY = 2;
    int ON_DEVICE_CONNECTED = 3;
    int ON_DEVICE_DISCONNECTED = 4;
    int ON_SCAN_START = 5;
    int ON_SCAN_STOP = 6;
    int ON_SCAN_START_FAILED = 7;
    int ON_SCAN_FILTERS_UPDATED = 8;
}
