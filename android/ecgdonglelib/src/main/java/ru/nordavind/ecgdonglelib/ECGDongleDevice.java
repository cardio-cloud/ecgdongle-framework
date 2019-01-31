package ru.nordavind.ecgdonglelib;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * describes connected device
 */
public class ECGDongleDevice implements Serializable {
    private static final String KEY_TYPE = "t";
    private static final String KEY_ID = "id";
    private static final String KEY_UUID = "uuid";

    /**
     * device type
     */
    public final ECGDongleType type;

    /**
     * string that identifies this device
     */
    public final String deviceId;

    /**
     * connected device UUID string, should match {@link ru.nordavind.ecgdonglelib.scan.DongleIdentity#uuid}
     * it's OK only on Android > 5.0 (21)
     * it is zero-filled on Android < 5.0, for Demo devices and for BLE devices
     * Can be null for older ECG Dongle Service versions
     */
    @Nullable
    public final String deviceUUID;

    public ECGDongleDevice(ECGDongleType type, String deviceId, String deviceUUID) {
        this.type = type;
        this.deviceId = deviceId;
        this.deviceUUID = deviceUUID;
    }

    public ECGDongleDevice(JSONObject src) throws JSONException {
        type = ECGDongleType.values()[src.getInt(KEY_TYPE)];
        deviceId = src.getString(KEY_ID);
        deviceUUID = src.optString(KEY_UUID);
    }

    public static List<ECGDongleDevice> fromJsonArray(JSONArray srcArr) throws JSONException {
        List<ECGDongleDevice> result = new ArrayList<>(srcArr.length());
        for (int i = 0; i < srcArr.length(); ++i) {
            result.add(new ECGDongleDevice(srcArr.getJSONObject(i)));
        }
        return result;
    }

    public static JSONArray toJsonArray(List<ECGDongleDevice> devices) throws JSONException {
        JSONArray result = new JSONArray();
        for (ECGDongleDevice device : devices) {
            result.put(device.toJsonObject());
        }
        return result;
    }

    public JSONObject toJsonObject() throws JSONException {
        return new JSONObject()
                .put(KEY_TYPE, type.ordinal())
                .put(KEY_ID, deviceId)
                .put(KEY_UUID, deviceUUID);
    }
}
