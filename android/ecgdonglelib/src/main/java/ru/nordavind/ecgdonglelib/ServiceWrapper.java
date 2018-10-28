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
    @Nullable
    OnDisconnectedFromServiceListener onDisconnectedFromServiceListener;
    private Messenger messenger = null; //used to make an RPC invocation
    private boolean isBound = false;
    private ServiceConnection connection;//receives callbacks from bind and unbind invocations
    private Messenger replyTo = null; //invocation replies are processed by this Messenger
    private Context context;
    private DataReplyReader dataReplyReader;
    @Nullable
    private OnConnectedToServiceListener onConnectedToServiceListener;
    @Nullable
    private OnGotDevicesListener onGotDevicesListener;
    @Nullable
    private OnDeviceConnectedListener onDeviceConnectedListener;
    @Nullable
    private OnDeviceDisconnectedListener onDeviceDisconnectedListener;
    @Nullable
    private OnScanStartedListener onScanStartedListener;
    @Nullable
    private OnNextDataReplyListener onNextDataReplyListener;
    @Nullable
    private OnScanStoppedListener onScanStoppedListener;
    @Nullable
    private OnFailedToStartScanListener onFailedToStartScanListener;
    @Nullable
    private OnScanFiltersUpdatedListener onScanFiltersUpdatedListener;

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
            if (onDisconnectedFromServiceListener != null)
                onDisconnectedFromServiceListener.onDisconnectedFromService();
        } else {
            if (connection != null)
                connection = null;
            if (replyTo != null)
                replyTo = null;
        }

    }

    @Override
    public IECGDongleServiceWrapper setOnConnectedToServiceListener(OnConnectedToServiceListener onConnectedToServiceListener) {
        this.onConnectedToServiceListener = onConnectedToServiceListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnGotDevicesListener(OnGotDevicesListener onGotDevicesListener) {
        this.onGotDevicesListener = onGotDevicesListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnDeviceConnectedListener(OnDeviceConnectedListener onDeviceConnectedListener) {
        this.onDeviceConnectedListener = onDeviceConnectedListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnDeviceDisconnectedListener(OnDeviceDisconnectedListener onDeviceDisconnectedListener) {
        this.onDeviceDisconnectedListener = onDeviceDisconnectedListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnScanStartedListener(OnScanStartedListener onScanStartedListener) {
        this.onScanStartedListener = onScanStartedListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnScanStoppedListener(OnScanStoppedListener onScanStoppedListener) {
        this.onScanStoppedListener = onScanStoppedListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnNextDataReplyListener(OnNextDataReplyListener onNextDataReplyListener) {
        this.onNextDataReplyListener = onNextDataReplyListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnFailedToStartScanListener(OnFailedToStartScanListener onFailedToStartScanListener) {
        this.onFailedToStartScanListener = onFailedToStartScanListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnScanFiltersUpdatedListener(OnScanFiltersUpdatedListener onScanFiltersUpdatedListenerListener) {
        this.onScanFiltersUpdatedListener = onScanFiltersUpdatedListenerListener;
        return this;
    }

    @Override
    public IECGDongleServiceWrapper setOnDisconnectedFromServiceListener(OnDisconnectedFromServiceListener onDisconnectedFromServiceListener) {
        this.onDisconnectedFromServiceListener = onDisconnectedFromServiceListener;
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
            if (onDisconnectedFromServiceListener != null)
                onDisconnectedFromServiceListener.onDisconnectedFromService();
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(TAG, "onBindingDied: ");
            ServiceWrapper.this.messenger = null;
            isBound = false;
            if (onDisconnectedFromServiceListener != null)
                onDisconnectedFromServiceListener.onDisconnectedFromService();
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
                case ServiceReplyId.CONNECT_REPLY:
                    String versionName = mp.getVersionName();
                    if (onConnectedToServiceListener != null && versionName != null) {
                        onConnectedToServiceListener.onConnectedToService(msg.arg1, msg.arg2, mp.getVersionCode(), versionName, mp.getSubscriptionState());
                    }
                    break;

                case ServiceReplyId.GET_DEVICES_REPLY:
                    if (onGotDevicesListener != null)
                        onGotDevicesListener.onGotDevices(mp.getDevices());
                    break;

                case ServiceReplyId.ON_DEVICE_CONNECTED:
                    if (onDeviceConnectedListener != null) {
                        ECGDongleDevice device = mp.getDevice();
                        List<ECGDongleDevice> devices = mp.getDevices();
                        if (device != null)
                            onDeviceConnectedListener.onDeviceConnected(device, devices);
                    }
                    break;

                case ServiceReplyId.ON_DEVICE_DISCONNECTED:
                    if (onDeviceDisconnectedListener != null) {
                        ECGDongleDevice device = mp.getDevice();
                        List<ECGDongleDevice> devices = mp.getDevices();
                        if (device != null)
                            onDeviceDisconnectedListener.onDeviceDisconnected(device, devices);
                    }
                    break;

                case ServiceReplyId.ON_SCAN_START:
                    if (onScanStartedListener != null) {
                        ECGDongleDevice device = mp.getDevice();
                        ScanConfig config = mp.getScanConfig();
                        if (device == null || config == null)
                            return;

                        dataReplyReader = new DataReplyReader(config, onNextDataReplyListener);
                        onScanStartedListener.onScanStarted(device, config);
                    }
                    break;

                case ServiceReplyId.ON_SCAN_STOP:
                    if (onScanStoppedListener != null) {
                        if (dataReplyReader != null) {
                            dataReplyReader.stop();
                            dataReplyReader = null;
                        }
                        ECGDongleDevice device = mp.getDevice();
                        ScanConfig config = mp.getScanConfig();
                        @DongleStopReason
                        int reason = msg.arg1;
                        if (device == null)
                            return;
                        onScanStoppedListener.onScanStopped(device, config, reason);
                    }
                    break;

                case ServiceReplyId.ON_SCAN_START_FAILED:
                    if (onFailedToStartScanListener != null) {
                        ECGDongleDevice device = mp.getDevice();
                        if (device == null)
                            return;
                        onFailedToStartScanListener.onFailedToStartScan(device, msg.arg1);
                    }
                    break;

                case ServiceReplyId.ON_SCAN_FILTERS_UPDATED:
                    ScanConfig config = mp.getScanConfig();
                    if (config != null) {
                        if (dataReplyReader != null) {
                            config = dataReplyReader.onFiltersChanged(config.getFilter());
                        }
                        if (onScanFiltersUpdatedListener != null) {
                            onScanFiltersUpdatedListener.onScanFiltersUpdated(config.getFilter(), config);
                        }
                    }
                    break;
            }
        }
    }
}
