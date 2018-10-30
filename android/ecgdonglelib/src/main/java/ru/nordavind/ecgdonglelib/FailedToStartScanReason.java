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
        FailedToStartScanReason.ERROR_ACCEPTING_SOCKET,
        FailedToStartScanReason.STOP_REQUESTED_BEFORE_SCAN_START,
        FailedToStartScanReason.EXCEPTION_WRITING_DATA_CHUNK,
        FailedToStartScanReason.DEVICE_STOPPED,
})

@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface FailedToStartScanReason {

    /**
     * device is not connected at the time ECG Dongle framework received StartScan message
     */
    int DEVICE_DISCONNECTED = 1;

    /**
     * Error creating scan runner. Some Framework issue
     */
    int SDK_ERROR_CANT_CREATE_SCANNER = 2;

    /**
     * error creating socket to transfer chunks between ECG Dongle Framework and ServiceWrapper
     */
    int SDK_ERROR_CANT_CREATE_SOCKET = 3;

    /**
     * Timed out while starting ECG Dongle scan
     */
    int SCAN_START_TIMED_OUT = 4;

    /**
     * got IO Exception when accepting socket used to transfer chunks between ECG Dongle Framework and ServiceWrapper
     */
    int ERROR_ACCEPTING_SOCKET = 6;

    /**
     * StopScan() was called before ECG Dongle Scan really started
     */
    int STOP_REQUESTED_BEFORE_SCAN_START = 7;

    /**
     * failed wo write DongleDataChunk to socket used to transfer chunks between ECG Dongle Framework and ServiceWrapper
     */
    int EXCEPTION_WRITING_DATA_CHUNK = 8;

    /**
     * active subscription is required to start scan with this device. No active subscruiption found
     */
    int NO_SUBSCRIPTION = 9;

    /**
     * device stopped before scan actually started. See {@link DongleStopReason}
     */
    int DEVICE_STOPPED = 10;

}
