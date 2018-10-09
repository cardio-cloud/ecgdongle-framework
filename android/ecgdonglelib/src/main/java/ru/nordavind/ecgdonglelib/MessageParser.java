package ru.nordavind.ecgdonglelib;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.nordavind.ecgdonglelib.filter.FilterSettings;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;

/**
 * parses messages used to send data between service and service wrapper lib
 */
public class MessageParser {

    public final Messenger replyTo;
    private final Bundle bundle;

    public MessageParser(Message message) {
        this.replyTo = message.replyTo;
        this.bundle = (Bundle) message.obj;
    }

    @Nullable
    public ECGDongleDevice getDevice() {
        String deviceStr = bundle.getString(MessageBuilder.KEY_DEVICE);
        try {
            return new ECGDongleDevice(new JSONObject(deviceStr));
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                throw new RuntimeException(e);
            return null;
        }
    }

    @NonNull
    public List<ECGDongleDevice> getDevices() {
        String devicesStr = bundle.getString(MessageBuilder.KEY_DEVICES);
        try {
            return ECGDongleDevice.fromJsonArray(new JSONArray(devicesStr));
        } catch (JSONException e) {
            if (BuildConfig.DEBUG)
                throw new RuntimeException(e);
            return new ArrayList<>();
        }
    }

    @Nullable
    public ScanConfig getScanConfig() {
        try {
            return new ScanConfig(new JSONObject(bundle.getString(MessageBuilder.KEY_SCAN_CONFIG)));
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                throw new RuntimeException(e);
            return null;
        }
    }

    @NonNull
    public FilterSettings getFilterSettings() {
        try {
            return new FilterSettings(new JSONObject(bundle.getString(MessageBuilder.KEY_FILTER)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getVersionCode() {
        if (bundle == null || !bundle.containsKey(MessageBuilder.KEY_VERSION_CODE))
            return 0;
        return bundle.getInt(MessageBuilder.KEY_VERSION_CODE);
    }

    @Nullable
    public String getVersionName() {
        if (bundle == null || !bundle.containsKey(MessageBuilder.KEY_VERSION_CODE))
            return null;
        return bundle.getString(MessageBuilder.KEY_VERSION_NAME);
    }

    public int getSubscriptionState() {
        if (bundle == null)
            return -1;


        return bundle.getInt(MessageBuilder.KEY_SUBSCRIPTION_STATE, -1);
    }
}