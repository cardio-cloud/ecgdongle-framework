package ru.nordavind.ecgdonglelib;

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

    /**
     * device type
     */
    public final ECGDongleType type;

    /**
     * string that identifies this device
     */
    public final String deviceId;

    public ECGDongleDevice(ECGDongleType type, String deviceId) {
        this.type = type;
        this.deviceId = deviceId;
    }

    public ECGDongleDevice(JSONObject src) throws JSONException {
        type = ECGDongleType.values()[src.getInt(KEY_TYPE)];
        deviceId = src.getString(KEY_ID);
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
                .put(KEY_ID, deviceId);
    }
}
