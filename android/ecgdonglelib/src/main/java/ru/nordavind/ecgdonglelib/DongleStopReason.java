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
        DongleStopReason.UNKNOWN,
        DongleStopReason.NO_REASON,
        DongleStopReason.BAD_DATA,
        DongleStopReason.DISCONNECTED_WHILE_COMMUNICATING,
        DongleStopReason.SHOULD_RECONNECT_DEVICE,
        DongleStopReason.DONGLE_STOPPED_SCANNING,
})
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface DongleStopReason {
    /**
     * Unknown reason; something unexpected
     */
    int UNKNOWN = -1;

    /**
     * no reason given;
     */
    int NO_REASON = 0;

    /**
     * ECG Dongle Framework got unexpected data from ECG Dongle
     */
    int BAD_DATA = 1;

    /**
     * ECG Dongle device was disconnected while scanning
     */
    int DISCONNECTED_WHILE_COMMUNICATING = 2;

    /**
     * due to some rare issues, it is required to reconnect ECG Dongle (it might be required up to 2 times)
     */
    int SHOULD_RECONNECT_DEVICE = 3;

    /**
     * normal reason to stop dongle
     */
    int DONGLE_STOPPED_SCANNING = 4;
}
