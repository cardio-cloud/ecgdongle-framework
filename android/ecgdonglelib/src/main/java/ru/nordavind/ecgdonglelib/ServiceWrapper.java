package ru.nordavind.ecgdonglelib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

import ru.nordavind.ecgdonglelib.filter.FilterSettings;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;

import static ru.nordavind.ecgdonglelib.Settings.TAG;

public class ServiceWrapper implements IECGDongleServiceWrapper {

    private static final String SERVICE_PACKAGE_NAME = "ru.nordavind.ecgdongleservice";
    private static final String SERVICE_NAME = "ru.nordavind.ecgdongleservice.ECGDongleService";
    private static final int PROTO_VERSION = 1;
    private volatile static IECGDongleServiceWrapper instance;

    private Messenger messenger = null; //used to make an RPC invocation
    private boolean isBound = false;
    private ServiceConnection connection;//receives callbacks from bind and unbind invocations
    private Messenger replyTo = null; //invocation replies are processed by this Messenger
    private Context context;
    private DataReplyReader dataReplyReader;

    @Nullable
    private WeakReference<OnConnectedToServiceListener> onConnectedToServiceListener;
    @Nullable
    private WeakReference<OnGotDevicesListener> onGotDevicesListener;
    @Nullable
    private WeakReference<OnDeviceConnectedListener> onDeviceConnectedListener;
    @Nullable
    private WeakReference<OnDeviceDisconnectedListener> onDeviceDisconnectedListener;
    @Nullable
    private WeakReference<OnScanStartedListener> onScanStartedListener;
    @Nullable
    private WeakReference<OnNextDataReplyListener> onNextDataReplyListener;
    @Nullable
    private WeakReference<OnScanStoppedListener> onScanStoppedListener;
    @Nullable
    private WeakReference<OnFailedToStartScanListener> onFailedToStartScanListener;
    @Nullable
    private WeakReference<OnScanFiltersUpdatedListener> onScanFiltersUpdatedListener;
    @Nullable
    private WeakReference<OnDisconnectedFromServiceListener> onDisconnectedFromServiceListener;

    public ServiceWrapper() {
    }

    public static IECGDongleServiceWrapper getInstance() {
        IECGDongleServiceWrapper local = instance;
        if (local == null) {
            synchronized (ServiceWrapper.class) {
                local = instance;
                if (local == null) {
                    local = instance = new ServiceWrapper();
                }
            }
        }
        return local;
    }

    private static void logMessage(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        StringBuilder builder = new StringBuilder();
        builder.append("incoming message: what: ").append(msg.what)
                .append(", arg1: ").append(msg.arg1)
                .append(", arg2: ").append(msg.arg2);
        if (bundle != null) {
            builder.append(", bundle:\n");
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                Object value = bundle.get(key);
                builder.append(key).append(": ").append(value).append("\n");
            }
        }
        Log.d(TAG, "got message: " + builder.toString());
    }

    private static <T> T getObject(@Nullable WeakReference<T> src) {
        return src == null ? null : src.get();
    }

    @Override
    public boolean isConnected() {
        return isBound && messenger != null;
    }

    @Override
    public int getProtoVersion() {
        return PROTO_VERSION;
    }

    @Override
    public Uri getGooglePlayServiceAppUri() {
        String url = "https://play.google.com/store/apps/details?id=" + SERVICE_PACKAGE_NAME;
        return Uri.parse(url);
    }

    @Override
    public boolean isServiceAppInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(SERVICE_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    @UiThread
    public boolean init(Context context) {
        if (isBound)
            return true;

        if (!isServiceAppInstalled(context))
            return false;

        this.context = context.getApplicationContext();
        if (connection == null)
            connection = new RemoteServiceConnection();
        if (replyTo == null)
            this.replyTo = new Messenger(new IncomingHandler());

        //Bind to the remote service
        Intent intent = new Intent();
        intent.setClassName(SERVICE_PACKAGE_NAME, SERVICE_NAME);

        if (!this.context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE)) {
            this.context.unbindService(connection);
            //connection = null;
            replyTo = null;
            isBound = false;
            return false;
        } else {
            isBound = true;
            return true;
        }
    }

    @Override
    public void release() {
        if (this.isBound) {
            new MessageBuilder(ServiceRequestId.STOP_LISTEN).send(messenger, replyTo);
            context.unbindService(connection);
            connection = null;
            replyTo = null;
            messenger = null;
            isBound = false;
            OnDisconnectedFromServiceListener callback = onDisconnectedFromServiceListener == null ?
                    null : onDisconnectedFromServiceListener.get();
            if (callback != null)
                callback.onDisconnectedFromService();
        } else {
            if (connection != null)
                connection = null;
            if (replyTo != null)
                replyTo = null;
        }

    }

    @Override
    public IECGDongleServiceWrapper setOnConnectedToServiceListener(OnConnectedToServiceListener onConnectedToServiceListener) {
        this.onConnectedToServiceListener = new WeakReference<>(onConnectedToServiceListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnGotDevicesListener(OnGotDevicesListener onGotDevicesListener) {
        this.onGotDevicesListener = new WeakReference<>(onGotDevicesListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnDeviceConnectedListener(OnDeviceConnectedListener onDeviceConnectedListener) {
        this.onDeviceConnectedListener = new WeakReference<>(onDeviceConnectedListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnDeviceDisconnectedListener(OnDeviceDisconnectedListener onDeviceDisconnectedListener) {
        this.onDeviceDisconnectedListener = new WeakReference<>(onDeviceDisconnectedListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnScanStartedListener(OnScanStartedListener onScanStartedListener) {
        this.onScanStartedListener = new WeakReference<>(onScanStartedListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnScanStoppedListener(OnScanStoppedListener onScanStoppedListener) {
        this.onScanStoppedListener = new WeakReference<>(onScanStoppedListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnNextDataReplyListener(OnNextDataReplyListener onNextDataReplyListener) {
        this.onNextDataReplyListener = new WeakReference<>(onNextDataReplyListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnFailedToStartScanListener(OnFailedToStartScanListener onFailedToStartScanListener) {
        this.onFailedToStartScanListener = new WeakReference<>(onFailedToStartScanListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnScanFiltersUpdatedListener(OnScanFiltersUpdatedListener onScanFiltersUpdatedListenerListener) {
        this.onScanFiltersUpdatedListener = new WeakReference<>(onScanFiltersUpdatedListenerListener);
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnDisconnectedFromServiceListener(OnDisconnectedFromServiceListener onDisconnectedFromServiceListener) {
        this.onDisconnectedFromServiceListener = new WeakReference<>(onDisconnectedFromServiceListener);
        return this;
    }

    @Override
    public boolean queryForDevices() {
        return isBound && new MessageBuilder(ServiceRequestId.GET_DEVICES).send(messenger, replyTo);
    }

    @Override
    public boolean startScan(ECGDongleDevice device, FilterSettings filterSettings) {
        return isBound && new MessageBuilder(ServiceRequestId.START_SCANNING)
                .setDevice(device).setFilter(filterSettings)
                .send(messenger, replyTo);
    }

    @Override
    public boolean setFilters(FilterSettings filterSettings) {
        return isBound && new MessageBuilder(ServiceRequestId.SET_FILTERS)
                .setFilter(filterSettings)
                .send(messenger, replyTo);
    }

    @Override
    public boolean stopCurrentScan() {
        if (isBound && new MessageBuilder(ServiceRequestId.STOP_SCANNING)
                .send(messenger, replyTo)) {
            return true;
        } else {
            if (dataReplyReader != null) {
                dataReplyReader.stop();
                dataReplyReader = null;
            }
        }
        return false;
    }

    private boolean sendStart() {
        return isBound && new MessageBuilder(ServiceRequestId.START)
                .send(messenger, replyTo);
    }

    private class RemoteServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName component, IBinder binder) {
            ServiceWrapper.this.messenger = new Messenger(binder);
            //ServiceWrapper.this.isBound = true;
            sendStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName component) {
            Log.e(TAG, "onServiceDisconnected: ");
            ServiceWrapper.this.messenger = null;
            isBound = false;
            OnDisconnectedFromServiceListener callback = onDisconnectedFromServiceListener == null ?
                    null : onDisconnectedFromServiceListener.get();
            if (callback != null)
                callback.onDisconnectedFromService();
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(TAG, "onBindingDied: ");
            ServiceWrapper.this.messenger = null;
            isBound = false;
            OnDisconnectedFromServiceListener callback = onDisconnectedFromServiceListener == null ?
                    null : onDisconnectedFromServiceListener.get();
            if (callback != null)
                callback.onDisconnectedFromService();
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (BuildConfig.DEBUG)
                logMessage(msg);

            @ServiceReplyId
            int what = msg.what;

            MessageParser mp = new MessageParser(msg);

            switch (what) {
                case ServiceReplyId.CONNECT_REPLY: {
                    String versionName = mp.getVersionName();
                    OnConnectedToServiceListener callback = getObject(onConnectedToServiceListener);
                    if (callback != null && versionName != null) {
                        callback.onConnectedToService(msg.arg1, msg.arg2, mp.getVersionCode(), versionName, mp.getSubscriptionState());
                    }
                    break;
                }


                case ServiceReplyId.GET_DEVICES_REPLY: {
                    OnGotDevicesListener callback = getObject(onGotDevicesListener);
                    if (callback != null)
                        callback.onGotDevices(mp.getDevices());
                    break;
                }

                case ServiceReplyId.ON_DEVICE_CONNECTED: {
                    OnDeviceConnectedListener callback = getObject(onDeviceConnectedListener);
                    if (callback != null) {
                        ECGDongleDevice device = mp.getDevice();
                        List<ECGDongleDevice> devices = mp.getDevices();
                        if (device != null)
                            callback.onDeviceConnected(device, devices);
                    }
                    break;
                }

                case ServiceReplyId.ON_DEVICE_DISCONNECTED: {
                    OnDeviceDisconnectedListener callback = getObject(onDeviceDisconnectedListener);
                    if (callback != null) {
                        ECGDongleDevice device = mp.getDevice();
                        List<ECGDongleDevice> devices = mp.getDevices();
                        if (device != null)
                            callback.onDeviceDisconnected(device, devices);
                    }
                    break;
                }

                case ServiceReplyId.ON_SCAN_START: {
                    OnScanStartedListener callback = getObject(onScanStartedListener);
                    OnNextDataReplyListener replyListener = getObject(onNextDataReplyListener);

                    if (replyListener != null) {
                        ECGDongleDevice device = mp.getDevice();
                        ScanConfig config = mp.getScanConfig();
                        if (device == null || config == null)
                            return;

                        dataReplyReader = new DataReplyReader(config, replyListener);
                        if (callback != null)
                            callback.onScanStarted(device, config);
                    }
                    break;
                }

                case ServiceReplyId.ON_SCAN_STOP: {
                    OnScanStoppedListener callback = getObject(onScanStoppedListener);
                    if (dataReplyReader != null) {
                        dataReplyReader.stop();
                        dataReplyReader = null;
                    }
                    if (callback != null) {
                        ECGDongleDevice device = mp.getDevice();
                        ScanConfig config = mp.getScanConfig();
                        @DongleStopReason
                        int reason = msg.arg1;
                        if (device == null)
                            return;
                        callback.onScanStopped(device, config, reason);
                    }
                    break;
                }

                case ServiceReplyId.ON_SCAN_START_FAILED: {
                    OnFailedToStartScanListener callback = getObject(onFailedToStartScanListener);
                    if (callback != null) {
                        ECGDongleDevice device = mp.getDevice();
                        if (device == null)
                            return;
                        callback.onFailedToStartScan(device, msg.arg1, msg.arg2);
                    }
                    break;
                }

                case ServiceReplyId.ON_SCAN_FILTERS_UPDATED: {
                    ScanConfig config = mp.getScanConfig();
                    if (config != null) {
                        if (dataReplyReader != null) {
                            config = dataReplyReader.onFiltersChanged(config.getFilter());
                        }
                        OnScanFiltersUpdatedListener callback = getObject(onScanFiltersUpdatedListener);
                        if (callback != null) {
                            callback.onScanFiltersUpdated(config.getFilter(), config);
                        }
                    }
                    break;
                }
            }
        }
    }
}
