package ru.nordavind.ecgdonglelib.scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Device's calibration settings
 */
public class SimpleCalibrationSettings implements Serializable {
    private static final String KEY_VERSION = "v";
    private static final String KEY_REJECT_FILTER = "r";
    private static final String KEY_CHANNELS_COUNT = "ch";
    private static final String KEY_B = "b";
    private static final String KEY_K = "k";
    private static final String KEY_MAX = "max";
    private static final String KEY_MIN = "min";
    private static final String KEY_DATA_FREQUENCY = "f";
    private static final String KEY_VALUES_IN_REPLY = "vr";
    private static final String KEY_SAMPLE_SIZE = "ss";
    private static final String KEY_DEVICE_TYPE_STR = "dtype";
    private static final String KEY_ABSENT_VALUE = "abs";
    private static final String KEY_CAN_ENABLE_SOFT_FILTERS = "softF";
    public final int channelsCount;
    private final double channelB[];
    private final double channelK[];

    private final int min;
    private final int max;
    private final int sampleSize;
    private final int absentValue;
    private final int dataFrequency;
    private final int valuesInReply;
    private final int version;
    private final StringDongleDeviceType deviceType;
    private final PowerFrequencyLib rejectFilter;
    private final boolean canEnableSoftwareFilters;

    public SimpleCalibrationSettings(int channelsCount, double[] channelB, double[] channelK,
                                     int min, int max, int sampleSize, int absentValue,
                                     int dataFrequency, int valuesInReply, int version,
                                     String deviceTypeName, int rejectFilterFrequencyHz,
                                     boolean canEnableSoftwareFilters) {
        this.channelsCount = channelsCount;
        this.channelB = channelB;
        this.channelK = channelK;
        this.min = min;
        this.max = max;
        this.sampleSize = sampleSize;
        this.absentValue = absentValue;
        this.dataFrequency = dataFrequency;
        this.valuesInReply = valuesInReply;
        this.version = version;
        this.deviceType = new StringDongleDeviceType(deviceTypeName);
        this.rejectFilter = PowerFrequencyLib.ofFrequencyHz(rejectFilterFrequencyHz);
        this.canEnableSoftwareFilters = canEnableSoftwareFilters;
    }

    public SimpleCalibrationSettings(JSONObject source) throws JSONException {
        rejectFilter = PowerFrequencyLib.fromInt(source.getInt(KEY_REJECT_FILTER));
        channelsCount = source.getInt(KEY_CHANNELS_COUNT);
        absentValue = source.getInt(KEY_ABSENT_VALUE);
        max = source.getInt(KEY_MAX);
        min = source.getInt(KEY_MIN);
        sampleSize = source.getInt(KEY_SAMPLE_SIZE);
        dataFrequency = source.getInt(KEY_DATA_FREQUENCY);
        valuesInReply = source.getInt(KEY_VALUES_IN_REPLY);
        version = source.getInt(KEY_VERSION);
        deviceType = new StringDongleDeviceType(source.getString(KEY_DEVICE_TYPE_STR));
        canEnableSoftwareFilters = source.getBoolean(KEY_CAN_ENABLE_SOFT_FILTERS);

        channelB = new double[channelsCount];
        channelK = new double[channelsCount];

        JSONArray arrB = source.getJSONArray(KEY_B);
        JSONArray arrK = source.getJSONArray(KEY_K);

        for (int i = 0; i < channelsCount; ++i) {
            channelB[i] = arrB.getDouble(i);
            channelK[i] = arrK.getDouble(i);
        }
    }

    public int getRejectionFilterFrequency() {
        return rejectFilter.getIntFrequency();
    }

    public String dump() {
        return "";/*String.format(Locale.US, "reject: %s, calibration2: %f. calibration3: %f, rejectF: %d, rejectQ: %d, max: %d, med: %d, ave: %d, freq: %d",
                rejectFilter.name(), calibration2, calibration3, rejectF, rejectQ, max, med, ave, dataFrequency);*/
    }

    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put(KEY_REJECT_FILTER, rejectFilter.toInt());
        res.put(KEY_CHANNELS_COUNT, channelsCount);
        res.put(KEY_ABSENT_VALUE, absentValue);
        res.put(KEY_MIN, min);
        res.put(KEY_MAX, max);
        res.put(KEY_SAMPLE_SIZE, sampleSize);
        res.put(KEY_DATA_FREQUENCY, dataFrequency);
        res.put(KEY_VALUES_IN_REPLY, valuesInReply);
        res.put(KEY_VERSION, version);
        res.put(KEY_DEVICE_TYPE_STR, getDeviceType().getPrintableName());
        res.put(KEY_CAN_ENABLE_SOFT_FILTERS, canEnableSoftwareFilters);


        JSONArray arr = new JSONArray();
        for (int i = 0; i < channelB.length; i++) {
            arr.put(channelB[i]);
        }
        res.put(KEY_B, arr);

        arr = new JSONArray();

        for (int i = 0; i < channelB.length; i++) {
            arr.put(channelK[i]);
        }
        res.put(KEY_K, arr);
        return res;
    }

    public int getDataFrequency() {
        return dataFrequency;
    }

    public int getValuesInReply() {
        return valuesInReply;
    }

    public double getMul(int channelNum) {
        return channelK[channelNum];
    }

    public int getMax() {
        return max;
    }

    public int getChannelsCount() {
        return channelsCount;
    }

    public double getZeroShift(int channelNum) {
        return channelB[channelNum];
    }

    public int getVersion() {
        return version;
    }

    public int getMin() {
        return min;
    }

    public boolean canEnableSoftwareFilters() {
        return canEnableSoftwareFilters;
    }

    public int getAbsentValue() {
        return absentValue;
    }

    public StringDongleDeviceType getDeviceType() {
        return deviceType;
    }

    public DongleChannelConfiguration getChannelConfiguration() {
        return DongleChannelConfiguration.getForChannelsCount(channelsCount);
    }

    public int getSampleSize() {
        return sampleSize;
    }
}