package ru.nordavind.ecgdonglelib;

/**
 * describes device types
 */
public enum ECGDongleType {
    /**
     * Unknown device type, reserved
     * update lib if you got this
     */
    Unknown,

    /**
     * demo device; actual device not required
     */
    Demo,
    /**
     * Old USB ECG Dongle
     */
    USBDongleV1,

    /**
     * new USB ECG Dongle
     */
    USBDongleV2
}
