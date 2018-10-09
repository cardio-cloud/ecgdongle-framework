package ru.nordavind.ecgdonglelib;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import java.util.List;

import ru.nordavind.ecgdonglelib.filter.FilterSettings;
import ru.nordavind.ecgdonglelib.filter.ScanFilterInfo;
import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;

/**
 * interface for ECG Dongle service wrapper
 */
public interface IECGDongleServiceWrapper {

    /**
     * @return true if connected to Service
     */
    boolean isConnected();

    /**
     * @return protocol version
     */
    int getProtoVersion();

    /**
     * call to initialise service connection
     *
     * @param context
     */
    @UiThread
    boolean init(Context context);

    /**
     * @return true if service app installed
     */
    boolean isServiceAppInstalled(Context context);

    /**
     * @return URI of ECG Dongle Service app in Google play
     */
    Uri getGooglePlayServiceAppUri();

    /**
     * call do release service connection
     */
    @UiThread
    void release();

    /**
     * query for connected devices
     *
     * @return true if request was sent to service
     */
    boolean queryForDevices();

    /**
     * start scanning with specified device
     *
     * @param device         Device to start scan with
     * @param filterSettings Filter settings to be used for scanning
     * @return true if request was sent to service
     */
    boolean startScan(ECGDongleDevice device, FilterSettings filterSettings);

    /**
     * sets filters for current scan
     * does nothing if no scan running
     *
     * @param filterSettings filter settings
     * @return true if request was sent to service
     */
    boolean setFilters(FilterSettings filterSettings);

    /**
     * stops current scan
     *
     * @return true if request was sent to service
     */
    boolean stopCurrentScan();

    IECGDongleServiceWrapper setOnGotDevicesListener(OnGotDevicesListener onGotDevicesListener);

    IECGDongleServiceWrapper setOnDeviceConnectedListener(OnDeviceConnectedListener onDeviceConnectedListener);

    IECGDongleServiceWrapper setOnDeviceDisconnectedListener(OnDeviceDisconnectedListener onDeviceDisconnectedListener);

    IECGDongleServiceWrapper setOnScanStartedListener(OnScanStartedListener onScanStartedListener);

    IECGDongleServiceWrapper setOnFailedToStartScanListener(OnFailedToStartScanListener onFailedToStartScanListener);

    IECGDongleServiceWrapper setOnScanStoppedListener(OnScanStoppedListener onScanStoppedListener);

    IECGDongleServiceWrapper setOnNextDataReplyListener(OnNextDataReplyListener onNextDataReplyListener);

    IECGDongleServiceWrapper setOnScanFiltersUpdatedListener(OnScanFiltersUpdatedListener onScanFiltersUpdatedListenerListener);

    IECGDongleServiceWrapper setOnConnectedToServiceListener(OnConnectedToServiceListener onConnectedToServiceListener);

    IECGDongleServiceWrapper setOnDisconnectedFromServiceListener(OnDisconnectedFromServiceListener onDisconnectedFromServiceListener);

    /**
     * listener to be called when connected to the service
     */
    interface OnConnectedToServiceListener {
        /**
         * called when connected to service
         *  @param minProtoVersion    min compatible protocol version
         * @param maxProtoVersion    max compatible protocol version
         * @param serviceVersionCode service app version code
         * @param serviceVersionName service app version name
         * @param subscriptionState
         */
        void onConnectedToService(int minProtoVersion, int maxProtoVersion, int serviceVersionCode, @Nullable String serviceVersionName, int subscriptionState);
    }

    /**
     * listener to be called when we get a list of connected devices (after connecting to service or after calling
     * {@link #queryForDevices }
     */
    interface OnGotDevicesListener {

        /**
         * @param connectedDevices Connected devices
         */
        @UiThread
        void onGotDevices(List<ECGDongleDevice> connectedDevices);
    }

    /**
     * listener to be called when a device is connected
     */
    interface OnDeviceConnectedListener {

        /**
         * @param connectedDevice  A device that is connected
         * @param connectedDevices All connected devices
         */
        @UiThread
        void onDeviceConnected(@NonNull ECGDongleDevice connectedDevice, @NonNull List<ECGDongleDevice> connectedDevices);
    }

    /**
     * listener to be called when a device is disconnected
     */
    interface OnDeviceDisconnectedListener {

        /**
         * @param disconnectedDevice A device that is disconnected
         * @param connectedDevices   All Connected devices
         */
        @UiThread
        void onDeviceDisconnected(@NonNull ECGDongleDevice disconnectedDevice, @NonNull List<ECGDongleDevice> connectedDevices);
    }

    /**
     * listener to be called when a scan is started
     */
    interface OnScanStartedListener {
        /**
         * @param device         ECG Dongle device used to start scan
         * @param scanConfig Configuration of the scan. contains information about channels, etc.
         */
        @UiThread
        void onScanStarted(@NonNull ECGDongleDevice device, @NonNull ScanConfig scanConfig);
    }

    /**
     * listener to be called when we failed to start scan
     */
    interface OnFailedToStartScanListener {
        /**
         * @param device ECG Dongle device used to start scan
         * @param reason Reason, why service failed to start scan (to be described later)
         */
        @UiThread
        void onFailedToStartScan(@NonNull ECGDongleDevice device, @FailedToStartScanReason int reason);
    }

    /**
     * listener to be called when scan filters changed
     */
    interface OnScanFiltersUpdatedListener {
        /**
         * @param filterInfo new filters
         */
        @UiThread
        void onScanFiltersUpdated(@NonNull ScanFilterInfo filterInfo, @NonNull ScanConfig scanConfig);
    }

    /**
     * listener to be called when scan stopped
     */
    interface OnScanStoppedListener {

        /**
         * @param device         ECG Dongle device used for the scan
         * @param scanConfig scanConfig of the scan
         */
        @UiThread
        void onScanStopped(@NonNull ECGDongleDevice device, @NonNull ScanConfig scanConfig);
    }

    /**
     * listener to be called when we've got next chunk of data from service
     */
    interface OnNextDataReplyListener {
        /**
         * @param dataReply DataReply that contains next data chunk for all channels
         */
        @WorkerThread
        void onNextDataReply(@NonNull DongleDataChunk dataReply);
    }

    /**
     * listener to be called when disconnected from service
     */
    interface OnDisconnectedFromServiceListener {
        void onDisconnectedFromService();
    }
}
