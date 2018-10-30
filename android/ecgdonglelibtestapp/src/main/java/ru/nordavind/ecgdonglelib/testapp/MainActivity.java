package ru.nordavind.ecgdonglelib.testapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.nordavind.ecgdonglelib.DongleStopReason;
import ru.nordavind.ecgdonglelib.ECGDongleDevice;
import ru.nordavind.ecgdonglelib.IECGDongleServiceWrapper;
import ru.nordavind.ecgdonglelib.ServiceWrapper;
import ru.nordavind.ecgdonglelib.filter.FilterSettings;
import ru.nordavind.ecgdonglelib.filter.ScanFilterInfo;
import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.scan.PowerFrequency;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;
import ru.nordavind.ecgdonglelib.storage.mitbih.DongleChunkSaverAsync;

public class MainActivity extends AppCompatActivity implements IECGDongleServiceWrapper.OnDeviceConnectedListener, IECGDongleServiceWrapper.OnDeviceDisconnectedListener, IECGDongleServiceWrapper.OnGotDevicesListener, IECGDongleServiceWrapper.OnScanStartedListener, IECGDongleServiceWrapper.OnScanStoppedListener, IECGDongleServiceWrapper.OnFailedToStartScanListener, IECGDongleServiceWrapper.OnNextDataReplyListener, IECGDongleServiceWrapper.OnScanFiltersUpdatedListener, IECGDongleServiceWrapper.OnConnectedToServiceListener, IECGDongleServiceWrapper.OnDisconnectedFromServiceListener {

    Button connectButton;
    Button getDevicesButton;
    Button scanButton;
    Button setFiltersButton;
    TextView resultView;
    ScrollView mainScroll;
    Spinner devicesSpinner;

    boolean connected;
    boolean scanning;

    Handler handler = new Handler();
    List<ECGDongleDevice> devices;
    DongleChunkSaverAsync saver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        getDevicesButton = findViewById(R.id.getDevicesButton);
        scanButton = findViewById(R.id.scanButton);
        resultView = findViewById(R.id.resultTextView);
        mainScroll = findViewById(R.id.scrollView);
        devicesSpinner = findViewById(R.id.devicesSpinner);
        setFiltersButton = findViewById(R.id.filtersButton);

        connectButton.setOnClickListener(this::onConnectButtonClick);
        getDevicesButton.setOnClickListener(this::onGetConnectedDevicesClick);
        scanButton.setOnClickListener(this::onStartStopScan);
        setFiltersButton.setOnClickListener(this::onSetFilters);

        ServiceWrapper.getInstance()
                .setOnConnectedToServiceListener(this)
                .setOnDeviceConnectedListener(this)
                .setOnDeviceDisconnectedListener(this)
                .setOnGotDevicesListener(this)
                .setOnScanStartedListener(this)
                .setOnScanStoppedListener(this)
                .setOnFailedToStartScanListener(this)
                .setOnNextDataReplyListener(this)
                .setOnScanFiltersUpdatedListener(this)
                .setOnDisconnectedFromServiceListener(this);

        Date date = new Date(System.currentTimeMillis());
        long dateL = Date.UTC(2018, 11, 1, 0, 0, 0);
        byte[] dat = new byte[8];
        ByteBuffer buf = ByteBuffer.wrap(dat).order(ByteOrder.LITTLE_ENDIAN);
        buf.putLong(dateL);
        buf.position(0);
        for (int i = 0; i < dat.length; i++) {
            dat[i] = (byte) (dat[i] ^ 38);
        }
    }

    private void onSetFilters(View view) {
        FilterSettings filterSettings = new FilterSettings(true, FilterSettings.UpperFrequency.Hz100, PowerFrequency.hz50, 30
        );
        addLogText("Setting filters...");
        ServiceWrapper.getInstance().setFilters(filterSettings);
    }

    private void onStartStopScan(View view) {
        if (scanning) {
            ServiceWrapper.getInstance().stopCurrentScan();
            scanning = false;
            addLogText("Stopping scan...");
            scanButton.setText("Start");
        } else {
            FilterSettings filterSettings = new FilterSettings(true, FilterSettings.UpperFrequency.Hz100, PowerFrequency.hz50, 30
            );

            int selected = devicesSpinner.getSelectedItemPosition();
            ServiceWrapper.getInstance().startScan(devices.get(selected), filterSettings);
            scanning = true;
            addLogText("Starting scan...");
            scanButton.setText("Stop");
        }
    }

    private void onGetConnectedDevicesClick(View view) {
        ServiceWrapper.getInstance().queryForDevices();
    }

    void onConnectButtonClick(View view) {
        if (connected) {
            disconnect();
        } else {
            connect();
        }
    }

    @Override
    public void onDeviceConnected(@NonNull ECGDongleDevice device, @NonNull List<ECGDongleDevice> connectedDevices) {
        onGotDevices(connectedDevices);
        addLogText(String.format("Device connected: %s, %s", device.type, device.deviceId));
    }

    @Override
    public void onDeviceDisconnected(@NonNull ECGDongleDevice device, @NonNull List<ECGDongleDevice> connectedDevices) {
        onGotDevices(connectedDevices);
        addLogText(String.format("Device disconnected: %s, %s", device.type, device.deviceId));
    }

    @Override
    public void onGotDevices(List<ECGDongleDevice> connectedDevices) {
        this.devices = connectedDevices;
        StringBuilder bld = new StringBuilder();
        bld.append("Connected devices: \n");
        for (ECGDongleDevice device : connectedDevices) {
            bld.append(String.format("\t %s: %s\n", device.type.name(), device.deviceId));
        }
        addLogText(bld);

        String[] names = new String[connectedDevices.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = connectedDevices.get(i).type.name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devicesSpinner.setAdapter(adapter);
    }

    @Override
    public void onScanStarted(@NonNull ECGDongleDevice device, @NonNull ScanConfig scanConfig) {
        addLogText("scan started");
    }

    @Override
    public void onScanStopped(@NonNull ECGDongleDevice device, @Nullable ScanConfig scanConfig, @DongleStopReason int stopReason) {
        addLogText("scan stopped: " + stopReason);
    }

    @Override
    public void onFailedToStartScan(@NonNull ECGDongleDevice device, int failedToStartReason, int stopReason) {
        addLogText("Failed to start scan: " + failedToStartReason);
    }

    @Override
    public void onNextDataReply(@NonNull DongleDataChunk dataChunk) {
        dataChunk.retain();
        handler.post(() -> {
            addLogText("got next data reply: " + dataChunk.getChunkNum());
            dataChunk.release();
        });
    }

    private void addLogText(CharSequence appendText) {
        SpannableStringBuilder builder;
        CharSequence text = resultView.getText();
        if (text instanceof SpannableStringBuilder)
            builder = (SpannableStringBuilder) text;
        else
            builder = new SpannableStringBuilder(text);

        //mainScroll.fullScroll(View.FOCUS_DOWN);
        if (builder.length() > 0 && appendText.charAt(appendText.length() - 1) != '\n')
            builder.insert(0, "\n");
        builder.insert(0, appendText);

        resultView.setText(builder);
    }

    private void connect() {
        if (!ServiceWrapper.getInstance().isServiceAppInstalled(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Alert")
                    .setMessage("Need to install lib app to work with ECG Dongle")
                    .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = ServiceWrapper.getInstance().getGooglePlayServiceAppUri();
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    })
                    .show();
        } else {
            ServiceWrapper.getInstance().init(this);
            addLogText("Initialising...");
        }
    }

    private void checkInstalled() {

    }

    private void disconnect() {
        ServiceWrapper.getInstance().release();
        addLogText("Releasing...");
    }

    @Override
    protected void onStop() {
        disconnect();
        super.onStop();
    }

    @Override
    public void onScanFiltersUpdated(@NonNull ScanFilterInfo filterInfo, @NonNull ScanConfig scanConfig) {
        addLogText("On filters changed");
    }

    @Override
    public void onConnectedToService(int minProtoVersion, int maxProtoVersion, int serviceVersionCode, @Nullable String serviceVersionName, int subscriptionState) {
        addLogText(String.format(Locale.US, "Connected; proto: %d-%d, srv: %d (%s) subs: %d", minProtoVersion, maxProtoVersion, serviceVersionCode, serviceVersionName, subscriptionState));
        connectButton.setText("Disconnect");
        connected = true;
    }

    @Override
    public void onDisconnectedFromService() {
        addLogText("DisconnectedFromService");

        connectButton.setText("Connect");
        scanButton.setText("Start");

        connected = false;
        scanning = false;
    }

    /**
     * example for saving chunks as Mit-Bih file
     *
     * @param context
     * @param config
     * @param filePath
     * @param exceptionListener
     */
    private void startSaving(@Nullable Context context, ScanConfig config, String filePath, @Nullable DongleChunkSaverAsync.ExceptionListener exceptionListener) {
        try {
            saver = new DongleChunkSaverAsync(context, config, filePath, exceptionListener);
        } catch (IOException e) {
            //TODO: do something
        }
    }

    private void saveChunk(DongleDataChunk chunk) {
        if (saver != null) {
            saver.addChunk(chunk);
        }
    }

    private void stopSaving() {
        if (saver != null) {
            saver.close();
            saver = null;
        }
    }
}
