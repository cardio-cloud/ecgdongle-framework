package ru.nordavind.ecgdonglelib;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import ru.nordavind.ecgdonglelib.filter.FilterSettings;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;

import static ru.nordavind.ecgdonglelib.Settings.TAG;

/**
 * builds messages to be sent between service and service wrapper lib
 */
public class MessageBuilder {
    static final String KEY_DEVICES = "dd";
    static final String KEY_DEVICE = "d";
    static final String KEY_FILTER = "filter";
    static final String KEY_SCAN_CONFIG = "scanConfig";
    static final String KEY_VERSION_CODE = "versionCode";
    static final String KEY_VERSION_NAME = "versionName";
    static final String KEY_SUBSCRIPTION_STATE = "subscribed";
    private final int what;
    private final int arg1;
    private final int arg2;

    private Bundle bundle = null;

    public MessageBuilder(int what) {
        this.what = what;
        arg1 = 0;
        arg2 = 0;
    }

    public MessageBuilder(int what, int arg1, int arg2) {
        this.what = what;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public Message build() {
        return Message.obtain(null, what, arg1, arg2, bundle);
    }

    public MessageBuilder setDevice(ECGDongleDevice device) {
        if (bundle == null)
            bundle = new Bundle();

        try {
            bundle.putString(KEY_DEVICE, device.toJsonObject().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public MessageBuilder setDevices(List<ECGDongleDevice> devices) {
        if (bundle == null)
            bundle = new Bundle();

        try {
            bundle.putString(KEY_DEVICES, ECGDongleDevice.toJsonArray(devices).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public MessageBuilder setFilter(FilterSettings filter) {
        if (bundle == null)
            bundle = new Bundle();

        try {
            bundle.putString(KEY_FILTER, filter.toJson().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public MessageBuilder setScanConfig(ScanConfig config) {
        if (bundle == null)
            bundle = new Bundle();

        try {
            bundle.putString(KEY_SCAN_CONFIG, config.toJson().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean send(Messenger messenger, Messenger replyTo) {
        if (messenger == null)
            return false;
        Message message = build();
        try {
            message.replyTo = replyTo;
            messenger.send(message);
        } catch (RemoteException rme) {
            Log.e(TAG, "send: ", rme);
            return false;
        }
        return true;
    }

    public boolean send(Messenger messenger) {
        if (messenger == null)
            return false;
        Message message = build();
        try {
            messenger.send(message);
        } catch (RemoteException rme) {
            Log.e(TAG, "send: ", rme);
            return false;
        }
        return true;
    }

    public void send(List<Messenger> eventListenerMessengers) {
        for (Messenger eventListenerMessenger : eventListenerMessengers) {
            send(eventListenerMessenger);
        }

    }

    public MessageBuilder setAppVersion(int versionCode, String versionName) {
        if (bundle == null)
            bundle = new Bundle();

        bundle.putInt(KEY_VERSION_CODE, versionCode);
        bundle.putString(KEY_VERSION_NAME, versionName);
        return this;
    }


    public MessageBuilder setSubscriptionState(int subsState) {
        if (bundle == null)
            bundle = new Bundle();

        bundle.putInt(KEY_SUBSCRIPTION_STATE, subsState);
        return this;
    }
}
