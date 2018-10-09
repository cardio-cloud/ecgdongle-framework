package ru.nordavind.ecgdonglelib;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * reasons why we were unable to start scan
 */
@IntDef({
        FailedToStartScanReason.DEVICE_DISCONNECTED,
        FailedToStartScanReason.SDK_ERROR_CANT_CREATE_SCANNER,
        FailedToStartScanReason.SDK_ERROR_CANT_CREATE_SOCKET,
        FailedToStartScanReason.SCAN_START_TIMED_OUT,
        FailedToStartScanReason.SOCKET_ACCEPT_TIMED_OUT,
        FailedToStartScanReason.ERROR_ACCEPTING_SOCKET,
        FailedToStartScanReason.STOP_REQUESTED_BEFORE_SCAN_START,
        FailedToStartScanReason.EXCEPTION_WRITING_DATA_REPLY,
})

@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface FailedToStartScanReason {
    int DEVICE_DISCONNECTED = 1;
    int SDK_ERROR_CANT_CREATE_SCANNER = 2;
    int SDK_ERROR_CANT_CREATE_SOCKET = 3;
    int SCAN_START_TIMED_OUT = 4;
    int SOCKET_ACCEPT_TIMED_OUT = 5;
    int ERROR_ACCEPTING_SOCKET = 6;
    int STOP_REQUESTED_BEFORE_SCAN_START = 7;
    int EXCEPTION_WRITING_DATA_REPLY = 8;

}
