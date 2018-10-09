package ru.nordavind.ecgdonglelib.scan;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

/**
 * Identity of a ECG Dongle device (UUID, firmware version etc.)
 */
public class DongleIdentity implements Serializable {
    private static final String KEY_UUID = "u";
    private static final String KEY_PART_ID = "p";
    private static final String KEY_BOOT = "b";
    private static final String KEY_FIRMWARE = "f";
    private static final String KEY_CHANNELS = "c";

    @NonNull
    public final UUID uuid;
    public final int partId;
    public final int bootVersion;
    public final int firmwareVersion;
    public final int channelsCount;

    public DongleIdentity(@NonNull UUID uuid, int partId, int bootVersion, int firmwareVersion, int channelsCount) {
        this.uuid = uuid;
        this.partId = partId;
        this.bootVersion = bootVersion;
        this.firmwareVersion = firmwareVersion;
        this.channelsCount = channelsCount;
    }

    public DongleIdentity(JSONObject source) throws JSONException {
        this.uuid = UUID.fromString(source.getString(KEY_UUID));
        this.partId = source.getInt(KEY_PART_ID);
        this.bootVersion = source.getInt(KEY_BOOT);
        this.firmwareVersion = source.getInt(KEY_FIRMWARE);
        this.channelsCount = source.getInt(KEY_CHANNELS);
    }

    public String dump() {
        return String.format(Locale.US, "%s, part: %d, boot: %d, firm: %d, ch: %d", uuid.toString(), partId, bootVersion, firmwareVersion, channelsCount);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_UUID, uuid.toString());
        jsonObject.put(KEY_PART_ID, partId);
        jsonObject.put(KEY_BOOT, bootVersion);
        jsonObject.put(KEY_FIRMWARE, firmwareVersion);
        jsonObject.put(KEY_CHANNELS, channelsCount);
        return jsonObject;
    }
}
