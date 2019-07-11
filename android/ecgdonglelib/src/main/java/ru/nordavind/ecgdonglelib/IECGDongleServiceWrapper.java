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
     * @return true if connected to ECG Dongle Framework service
     */
    boolean isConnected();

    /**
     * @return protocol version
     */
    int getProtoVersion();

    /**
     * Call this method to bind ServiceWrapper to ECG Dongle Framework service.
     * {@link OnConnectedToServiceListener} is called when ServiceWrapper connected to
     * ECG Dongle Framework service.
     * Then ECG Dongle Framework service sends list of currently connected ECG Dongle devices and
     * {@link OnGotDevicesListener} is called.
     *
     * @param context
     */
    @UiThread
    boolean init(Context context);

    /**
     * @return true if ECG Dongle Framework application installed
     *
     */
    boolean isServiceAppInstalled(Context context);

    /**
     * @return URI of ECG Dongle Service app in Google play
     *
     * Open the Uri:
     *     Uri uri = getGooglePlayServiceAppUri();
     *     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     *     context.startActivity(intent);
     */
    Uri getGooglePlayServiceAppUri();

    /**
     * call do release ECG Dongle Framework service connection
     *
     * {@link OnDisconnectedFromServiceListener#onDisconnectedFromService()} callback is called
     * after disconnecting from ECG Dongle Framework service.
     */
    @UiThread
    void release();

    /**
     * query for connected devices
     *
     * @return true if request was sent to service
     *
     * {@link OnGotDevicesListener#onGotDevices(List)} callback is called un UI thread after
     * ECG Dongle Framework returns a list of connected ECG Dongle devices
     */
    boolean queryForDevices();

    /**
     * start scanning with specified device
     *
     * @param device         Device to start scan with
     * @param filterSettings Filter settings to be used for scanning
     * @return true if request was sent to service
     *
     * {@link OnConnectedToServiceListener#onConnectedToService(int, int, int, String, int)}
     * callback is called after successful ECG Dongle scan start.
     *
     * If ECG Dongle Framework fails to start scan,
     * {@link OnFailedToStartScanListener#onFailedToStartScan(ECGDongleDevice, int, int)} callback
     * is called on UI thread
     * use {@link FailedToStartScanReason} and {@link DongleStopReason} to determine/report, why
     * ECG Dongle Framework service failed to start scan.
     * Note: if failedToStartReason == {@link FailedToStartScanReason#DEVICE_STOPPED} and
     * stopReason == {@link DongleStopReason#SHOULD_RECONNECT_DEVICE}, please, ask user to reconnect
     * the ECG Dongle device. It might be required to reconnect ECG Dongle up to 3 times on some
     * rare Android devices.
     */
    boolean startScan(ECGDongleDevice device, FilterSettings filterSettings);

    /**
     * same as {@link #startScan(ECGDongleDevice, FilterSettings)},
     * but will fail to start if accessCode does not match
     * <p>
     * If access code didn't match, the
     * {@link OnFailedToStartScanListener#onFailedToStartScan(ECGDongleDevice, int, int)}
     * called with failedToStartReason = {@link FailedToStartScanReason#BAD_ACCESS_CODE}
     *
     * @param device
     * @param filterSettings
     * @param accessCode
     * @return
     */
    boolean startScan(ECGDongleDevice device, FilterSettings filterSettings, long accessCode);

    /**
     * sets filters for current scan
     * does nothing if no scan running
     *
     * @param filterSettings filter settings
     * @return true if request was sent to service
     *
     * {@link OnScanFiltersUpdatedListener#onScanFiltersUpdated(ScanFilterInfo, ScanConfig)}
     * callback is called after filters actually changed
     */
    boolean setFilters(FilterSettings filterSettings);

    /**
     * stops current scan
     * does nothing if no scan running
     *
     * @return true if request was sent to service
     *
     * {@link OnScanStoppedListener#onScanStopped(ECGDongleDevice, ScanConfig, int)} callback
     * is called after scan actually stopped.
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
         *
         * @param minProtoVersion    min compatible protocol version
         * @param maxProtoVersion    max compatible protocol version
         * @param serviceVersionCode service app version code
         * @param serviceVersionName service app version name
         * @param subscriptionState
         */
        @UiThread
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
         * @param device     ECG Dongle device used to start scan
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
         * @param device     ECG Dongle device used to start scan
         * @param failedToStartReason     Reason, why service failed to start scan (to be described later)
         * @param stopReason if reason = DEVICE_STOPPED,
         */
        @UiThread
        void onFailedToStartScan(@NonNull ECGDongleDevice device, @FailedToStartScanReason int failedToStartReason, @DongleStopReason int stopReason);
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
     * listener to be called when scan stopped.
     * Scan stop might occur after  {@link IECGDongleServiceWrapper#stopCurrentScan()} called or in
     * some other situations. For example, whe ECG Dongle device is disconnected or if
     * ECG Dongle Framework received invalid data from ECG Dongle device.
     */
    interface OnScanStoppedListener {

        /**
         * @param device     ECG Dongle device used for the scan
         * @param scanConfig scanConfig of the scan
         * @param stopReason see {@link DongleStopReason} to determine, why scan was stopped.
         */
        @UiThread
        void onScanStopped(@NonNull ECGDongleDevice device, @Nullable ScanConfig scanConfig, @DongleStopReason int stopReason);
    }

    /**
     * listener to be called when we've got next chunk of data from ECG Dongle Framework
     */
    interface OnNextDataReplyListener {
        /**
         * @param dataChunk object that contains next data chunk for all channels
         */
        @WorkerThread
        void onNextDataReply(@NonNull DongleDataChunk dataChunk);
    }

    /**
     * listener to be called when disconnected from ECG Dongle Framework service.
     * Note: unbinding occurs after calling release() or if ECG Dongle Framework service crashed
     * or get updated by Google Play, of if Android system killed it for some reason.
     */
    interface OnDisconnectedFromServiceListener {
        void onDisconnectedFromService();
    }
}
