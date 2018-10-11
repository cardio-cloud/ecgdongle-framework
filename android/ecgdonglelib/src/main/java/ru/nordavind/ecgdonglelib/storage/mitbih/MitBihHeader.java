package ru.nordavind.ecgdonglelib.storage.mitbih;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import ru.nordavind.ecgdonglelib.scan.ChannelDescriptorLib;
import ru.nordavind.ecgdonglelib.scan.Lead;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;
import ru.nordavind.ecgdonglelib.util.JsHelper;

/**
 * Created by Babay on 18.12.2014.
 */
public class MitBihHeader implements Cloneable {
    private static final String RESEARCH_CONFIG = "rc";
    private static final String FILE_NAMES = "fn";
    private static final String CHANNEL_NAMES = "cn";
    private static final String AMOUNT = "a";
    private static final String START_TIME = "st";
    private static final String FORMATS = "ff";
    private static final String VERSION = "v";
    private static final String BAD_READINGS = "badReadings";
    final ScanConfig scanConfig;
    final String filePath;
    final ChannelDescriptorLib[] channels;
    private final Lead[] leads;
    private final String[] channelFileNames;
    private final long startTime;
    MitBihFormat[] formats;
    int[] crc;
    int missingReadings;
    private String shortName; // file name without path but with extension (.hea)
    private int version = 2;
    private int numberOfSamplesPerSignal = 0;

    public MitBihHeader(ScanConfig config, String filePath, String[] channelFileNames, MitBihFormat format, Lead[] leads, long startTime) {
        this.scanConfig = config;
        this.channels = config.getChannelDescriptors();
        this.filePath = filePath;
        this.channelFileNames = channelFileNames;
        if (leads == null) {
            this.leads = scanConfig.getHardwareLeads();
        } else {
            this.leads = leads;
        }
        File file = new File(filePath);
        shortName = file.getName();
        this.startTime = startTime;

        formats = new MitBihFormat[channels.length];
        for (int i = 0; i < channels.length; i++)
            formats[i] = format;

        crc = new int[channels.length];
    }

    MitBihHeader(JSONObject jsonObject, String filePath) throws JSONException {
        this.filePath = filePath;
        scanConfig = new ScanConfig(jsonObject.getJSONObject(RESEARCH_CONFIG));
        channels = scanConfig.getChannelDescriptors();
        channelFileNames = JsHelper.stringsFromJSA(jsonObject.getJSONArray(FILE_NAMES));
        numberOfSamplesPerSignal = jsonObject.getInt(AMOUNT);
        startTime = jsonObject.getLong(START_TIME);
        formats = JsHelper.mbfsFromJSA(jsonObject.getJSONArray(FORMATS));
        version = jsonObject.getInt(VERSION);
        leads = new Lead[channels.length];
        for (int i = 0; i < leads.length; i++) {
            leads[i] = scanConfig.getChannelDescriptors()[i].getLead();
        }
        missingReadings = jsonObject.has(BAD_READINGS) ? jsonObject.getInt(BAD_READINGS) : 0;
    }

    public static MitBihHeader make(File outputFile, ScanConfig config, MitBihFormat format, long startTime) {
        Lead storeLeads[] = config.getHardwareLeads();
        String[] channelFileNames = new String[storeLeads.length];

        for (int i = 0; i < storeLeads.length; i++) {
            channelFileNames[i] = "ch01";
        }

        return new MitBihHeader(config, outputFile.getPath(), channelFileNames, format, storeLeads, startTime);
    }

    public static MitBihHeader readHeader(InputStream inputStream, String path) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String firstLine = r.readLine();
        if (firstLine == null)
            return null;
        total.append(firstLine);
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
            total.append("\n");
        }
        int sharpPos = firstLine.indexOf("#");
        String jsStr = firstLine.substring(sharpPos + 1);
        MitBihHeader header = null;
        try {
            header = new MitBihHeader(new JSONObject(jsStr), path);
        } catch (JSONException e) {
            Log.e("MitBihHeader", e.getLocalizedMessage(), e);
            return null;
        }
        return header;
    }

    @Override
    public MitBihHeader clone() {
        try {
            MitBihHeader copy = (MitBihHeader) super.clone();
            copy.crc = crc.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getChannelFileName(int num) {
        return channelFileNames[num] + ".dat";
    }

    public String getChannelDataName(int num) {
        return channelFileNames[num] + ".dat";
    }

    public void writeTo(OutputStream stream, boolean withJsonConfig) throws IOException {
        stream.write(getHeaderString(withJsonConfig).getBytes());
    }

    /**
     * returns header config as described at:
     * http://www.physionet.org/physiotools/wag/header-5.htm
     *
     * @return
     */

    public String getHeaderString(boolean withJsonConfig) {
        StringBuilder builder = new StringBuilder();
        builder.append(getMainHeaderString(withJsonConfig));
        for (int i = 0; i < leads.length; i++) {
            builder.append(getChannelHeaderString(i));
        }

        builder.append("\n");

        if (scanConfig.getFilter() != null && scanConfig.getFilter().isFilteringEnabled()) {
            builder.append("\n# Filter: ").append(scanConfig.getFilter().describeEn());
        }
        return builder.toString();
    }

    private String getChannelHeaderString(int num) {
        ChannelDescriptorLib channel = channels[num];

        StringBuilder builder = new StringBuilder();
        short crc = (short) (this.crc[num] & 0xffff);
        builder.append(getChannelFileName(num))
                .append(" ").append(formats[num].getName()).append(" ") // format
                .append(String.format(Locale.US, "%f", channel.getAdcGain()))
                .append("(0)")// baseLine - we have zero baseline; unsigned values already converted to signed by subtraction zeroShift
                //.append(String.format("(%d)", channel.getZeroShift()))// baseLine
                .append("/mV ")
                .append(String.format(Locale.US, "%d", channel.getSampleSize()))
                .append(" ")
                .append(String.format(Locale.US, "%d", channel.getAdcMiddle())) // center of measurement interval
                .append(" 0 ") // initial value - not used
                .append(String.format(Locale.US, "%d", crc)) // checksum
                .append(" 0 ") //block size
                .append(leads[num].name()) // channel name
                .append("\n");
        return builder.toString();
    }

    private String getMainHeaderString(boolean withJsonConfig) {
        long date = startTime;
        try {
            return String.format(Locale.US, "%s %d %.0f %d %tH:%tM:%tS %td/%tm/%tY #%s\n",
                    shortName,
                    leads.length,
                    channels[0].getFrequency(),
                    numberOfSamplesPerSignal,
                    date, date, date, // time
                    date, date, date // date
                    , withJsonConfig ? toJson() : ""
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    JSONObject toJson() throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put(RESEARCH_CONFIG, scanConfig.toJson());
        jso.put(FILE_NAMES, JsHelper.toJSA(channelFileNames));
        jso.put(CHANNEL_NAMES, JsHelper.toJSA(leads));
        jso.put(AMOUNT, numberOfSamplesPerSignal);
        jso.put(START_TIME, startTime);
        jso.put(FORMATS, JsHelper.toJSA(formats));
        jso.put(VERSION, version);

        jso.put(BAD_READINGS, missingReadings);

        return jso;
    }

    public String getHeaderShortName() {
        return shortName.endsWith(".hea") ? shortName : shortName + ".hea";
    }

    /**
     * call after some values written on all channels
     *
     * @param amount -- amount of values written to each channel
     */
    public void onValuesWritten(int amount) {
        this.numberOfSamplesPerSignal += amount;
    }
}
