package ru.nordavind.ecgdonglelib.scan;

import java.io.Serializable;

/**
 * describes ECG Dongle device type
 */
public class StringDongleDeviceType implements Serializable {
    public final String deviceType;

    public StringDongleDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPrintableName() {
        return deviceType;
    }
}