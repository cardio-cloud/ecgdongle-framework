package ru.nordavind.ecgdonglelib.scan;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import ru.nordavind.ecgdonglelib.filter.ScanFilterInfo;

/**
 * Configuration of a scan.
 * Lead samples are organized in chunks.
 * Each chunk hold some samples for all hardware leads and can calculate values for hardware and calculated leads.
 * Scan config holds all info for scan except chunks.
 */
public class ScanConfig implements Serializable {
    private static final String KEY_FILTER = "fltr";
    private static final String APP_VERSION = "appv";
    private static final String KEY_DONGLE_IDENTITY = "id";
    private static final String KEY_CALIBRATION = "c";
    private static final String KEY_SOCKET_NAME = "s";

    /**
     * app version (service dongle)
     */
    public final String appVersion;
    /**
     * channel configuration of current ECG Dongle device.
     * IDs for hardware and software leads.
     */
    public final DongleLeadConfiguration channelConfiguration;
    /**
     * socket name to connect to
     */
    public final String socketName;
    /**
     * ECG Dongle identity device id, firmware version, channel count etc.
     */
    protected final DongleIdentity ecgDongleIdentity;
    /**
     * channel descriptors for hardware leads;
     * each ChannelDescriptor holds all info about channel samples:
     * frequency, period, chunk size, coefficients required to calculate values from samples
     */
    final ChannelDescriptorLib[] descriptors;
    /**
     * hardware/software filters info
     */

    protected ScanFilterInfo filter;

    public ScanConfig(String appVersion, SimpleCalibrationSettings calibrationSettings, DongleIdentity ecgDongleIdentity, ScanFilterInfo filter, String socketName) {
        this.appVersion = appVersion;
        this.calibrationSettings = calibrationSettings;
        this.channelConfiguration = calibrationSettings.getChannelConfiguration();
        this.ecgDongleIdentity = ecgDongleIdentity;
        this.socketName = socketName;
        this.filter = filter;
        this.descriptors = ChannelDescriptorLib.createDescriptors(calibrationSettings);
    }

    /**
     * device calibration settings
     */
    private SimpleCalibrationSettings calibrationSettings;

    public ScanConfig(JSONObject source) throws JSONException {
        if (!source.isNull(APP_VERSION)) {
            appVersion = source.getString(APP_VERSION);
        } else
            appVersion = "";

        if (!source.isNull(KEY_FILTER)) {
            this.filter = new ScanFilterInfo(source.getJSONObject(KEY_FILTER));
        }

        this.calibrationSettings = new SimpleCalibrationSettings(source.getJSONObject(KEY_CALIBRATION));
        this.channelConfiguration = calibrationSettings.getChannelConfiguration();
        this.ecgDongleIdentity = new DongleIdentity(source.getJSONObject(KEY_DONGLE_IDENTITY));
        this.socketName = source.getString(KEY_SOCKET_NAME);

        this.descriptors = ChannelDescriptorLib.createDescriptors(calibrationSettings);
    }

    public final ChannelDescriptorLib[] getDescriptors() {
        return descriptors;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(APP_VERSION, appVersion);
        jsonObject.put(KEY_DONGLE_IDENTITY, ecgDongleIdentity.toJson());
        jsonObject.put(KEY_CALIBRATION, calibrationSettings.toJson());
        jsonObject.put(KEY_SOCKET_NAME, socketName);
        if (filter != null) {
            jsonObject.put(KEY_FILTER, filter.toJson());
        }

        return jsonObject;
    }

    /**
     * @return channel descriptors
     */
    public ChannelDescriptorLib[] getChannelDescriptors() {
        return descriptors;
    }

    /**
     * see {@link DongleLeadConfiguration#getLeads()}
     */
    public Lead[] getLeads() {
        return channelConfiguration.getLeads();
    }

    /**
     * @return only hardware leads (source), not calculated leads
     */

    public Lead[] getHardwareLeads() {
        return channelConfiguration.getHardwareLeads();
    }

    /**
     * @return filter information of the scan
     */
    public ScanFilterInfo getFilter() {
        return filter;
    }

    /**
     * updates filter information of the scan config
     *
     * @param filter
     */
    public void setFilter(ScanFilterInfo filter) {
        this.filter = filter;
    }

    /**
     * @return ECG Dongle's calibration settings
     */
    public SimpleCalibrationSettings getCalibrationSettings() {
        return calibrationSettings;
    }

    /**
     * @return true if we can enable software filters for the ECG Dongle
     */
    public boolean canEnableSoftwareFilters() {
        return calibrationSettings.canEnableSoftwareFilters();
    }

    /**
     * @return int buffer size used to transfer single data chunk between service and ServiceWrapper
     */
    public int getBufferSizeForDataReply() {
        int size = 3;
        for (ChannelDescriptorLib descriptor : descriptors) {
            size += descriptor.bufferSize;
        }
        return size * 4;
    }
}
