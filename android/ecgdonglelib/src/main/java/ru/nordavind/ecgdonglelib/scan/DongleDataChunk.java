package ru.nordavind.ecgdonglelib.scan;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import ru.nordavind.ecgdonglelib.util.IRecycleable;
import ru.nordavind.ecgdonglelib.util.ObjectRecycler;

/**
 * Data chunk;
 * Holds sample values for all hardware channels for a time interval
 */
public class DongleDataChunk implements IRecycleable {
    public static final int PACKET_START_MARKER = 0xfe01fe02;

    private final int channelsCount;
    private final int valuesInReply;
    private final ScanConfig config;
    private final ObjectRecycler<DongleDataChunk> replyRecycler;
    private final AtomicInteger retainCounter = new AtomicInteger();
    private int chunkNum;
    private int[][] mccData;
    private int lostValues = 0;

    public DongleDataChunk(ScanConfig scanConfig, ObjectRecycler<DongleDataChunk> recycler) {
        this.config = scanConfig;
        this.replyRecycler = recycler;
        this.channelsCount = scanConfig.descriptors.length;
        this.valuesInReply = scanConfig.descriptors[0].bufferSize;
        this.mccData = new int[channelsCount][valuesInReply];
    }

    /**
     * it is possible that some data was not received from ECG Dongle
     * if that happens, values is replaced with {@link SimpleCalibrationSettings#getAbsentValue()}
     * @return amount of bad values in this chunk
     */
    public int getBadValuesAmount() {
        return lostValues;
    }

    /**
     * @return raw data (samples) for all hardware channels
     */
    public int[][] getRawData() {
        return mccData;
    }

    /**
     * @return chunk number (0-based)
     */
    public int getChunkNum() {
        return chunkNum;
    }

    /**
     * @return duration of chunk in ms
     */
    public int getDuration() {
        return config.getChannelDescriptors()[0].getBufferDurationMs();
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s : #%d", getClass().getSimpleName(), chunkNum);
    }

    /**
     * @return chunk start relative to scan start
     */
    public long getChunkStart() {
        return chunkNum * config.descriptors[0].getBufferDurationMs();
    }

    /**
     * see {@link IRecycleable}
     */
    @Override
    public void retain() {
        retainCounter.incrementAndGet();
    }

    /**
     * see {@link IRecycleable}
     */
    @Override
    public void release() {
        if (retainCounter.decrementAndGet() <= 0 && replyRecycler != null) {
            replyRecycler.recycle(this);
        }
    }

    /**
     * @return amount of samples for each channel in this chunk
     */
    public int getValuesInReply() {
        return valuesInReply;
    }

    /**
     * return calculated data for specified lead
     *
     * @param lead   lead to get data
     * @param buffer will be filled with calculated values
     */
    public void getCalculatedData(Lead lead, float[] buffer) {
        getCalculatedData(lead, 0, mccData[0].length, buffer);
    }

    /**
     * return calculated data for specified lead
     *
     * @param lead   lead to get data
     * @param start  index of first sample to be processed, zero-based;
     *               value calculated with sample[start] will have index 0 in buffer
     * @param end    index next to last sample that should be processed
     * @param buffer will be filled with calculated values
     */
    public void getCalculatedData(Lead lead, int start, int end, float[] buffer) {
        int hwLeadNum = config.getCalibrationSettings().getChannelConfiguration().getHardwareLeadNum(lead);
        if (hwLeadNum >= 0) {
            final int[] data = mccData[hwLeadNum];
            final float mul = config.descriptors[hwLeadNum].getFloatMult();
            final float shift = (float) config.descriptors[hwLeadNum].getZeroShift();
            for (int i = start, j = 0; i < end; i++, j++) {
                buffer[j] = (data[i] + shift) * mul;
            }
            return;
        }

        final int[] ch0 = mccData[0];
        final int[] ch1 = mccData[1];

        final float mul0 = config.descriptors[0].getFloatMult();
        final float mul1 = config.descriptors[1].getFloatMult();
        final float shift0 = (float) config.descriptors[0].getZeroShift();
        final float shift1 = (float) config.descriptors[1].getZeroShift();

        switch (lead) {
            case I:
                for (int i = start, j = 0; i < end; i++, j++) {
                    buffer[j] = (ch0[i] + shift0) * mul0 - (ch1[i] + shift1) * mul1;
                }
                return;

            case aVR:
                for (int i = start, j = 0; i < end; i++, j++) {
                    //aVR = -(II+I)/2 = -(ch0 + ch0 - ch1) / 2 =  ch1/2 - ch0
                    buffer[j] = (ch1[i] + shift1) * mul1 / 2 - (ch0[i] + shift0) * mul0;
                }
                return;

            case aVL:
                //aVL = I-II/2 = (ch0 - ch1) - ch0 / 2 = ch0/2 - ch1
                for (int i = start, j = 0; i < end; i++, j++) {
                    buffer[j] = ((ch0[i] + shift0) * mul0) / 2 - (ch1[i] + shift1) * mul1;
                    //buffer[j] = -(ch1[i] - zeroShift1) * mul1 / 2;
                }
                return;

            case aVF:
                for (int i = start, j = 0; i < end; i++, j++) {
                    //aVF = II-I/2 = ch0 - (ch0 - ch1)/ 2 = (ch0 + ch1)/2
                    buffer[j] = ((ch0[i] + shift0) * mul0 + (ch1[i] + shift1) * mul1) / 2;
                }
                return;
        }
    }

    /**
     * return calculated data for specified lead
     *
     * @param lead   lead to get data
     * @param start  index of first sample to be processed, zero-based;
     *               value calculated with sample[start] will have index 0 in buffer
     * @param end    index next to last sample that should be processed
     * @param buffer will be filled with calculated values
     */
    public void getCalculatedData(Lead lead, int start, int end, double[] buffer) {
        SimpleCalibrationSettings settings = config.getCalibrationSettings();
        int hwLeadNum = settings.getChannelConfiguration().getHardwareLeadNum(lead);
        if (hwLeadNum >= 0) {
            final int[] data = mccData[hwLeadNum];
            final double mul = config.descriptors[hwLeadNum].getDoubleMult();
            final double shift = config.descriptors[hwLeadNum].getZeroShift();
            for (int i = start, j = 0; i < end; i++, j++) {
                buffer[j] = (data[i] + shift) * mul;
            }
            return;
        }

        final int[] ch0 = mccData[0];
        final int[] ch1 = mccData[1];

        final double mul0 = config.descriptors[0].getDoubleMult();
        final double mul1 = config.descriptors[1].getDoubleMult();
        final double shift0 = config.descriptors[0].getZeroShift();
        final double shift1 = config.descriptors[1].getZeroShift();

        switch (lead) {
            case I:
                for (int i = start, j = 0; i < end; i++, j++) {
                    buffer[j] = (ch0[i] + shift0) * mul0 - (ch1[i] + shift1) * mul1;
                }
                return;

            case aVR:
                for (int i = start, j = 0; i < end; i++, j++) {
                    //aVR = -(II+I)/2 = -(ch0 + ch0 - ch1) / 2 =  ch1/2 - ch0
                    buffer[j] = (ch1[i] + shift1) * mul1 / 2 - (ch0[i] + shift0) * mul0;
                }
                return;

            case aVL:
                //aVL = I-II/2 = (ch0 - ch1) - ch0 / 2 = ch0/2 - ch1
                for (int i = start, j = 0; i < end; i++, j++) {
                    buffer[j] = ((ch0[i] + shift0) * mul0) / 2 - (ch1[i] + shift1) * mul1;
                    //buffer[j] = -(ch1[i] - zeroShift1) * mul1 / 2;
                }
                return;

            case aVF:
                for (int i = start, j = 0; i < end; i++, j++) {
                    //aVF = II-I/2 = ch0 - (ch0 - ch1)/ 2 = (ch0 + ch1)/2
                    buffer[j] = ((ch0[i] + shift0) * mul0 + (ch1[i] + shift1) * mul1) / 2;
                }
                return;
        }
    }

    /**
     * dumps chunk to string
     *
     * @return dumped string
     */
    public String dump() {
        StringBuilder builder = new StringBuilder();
        builder.append(" #").append(chunkNum);

        for (int i = 0; i < channelsCount; i++) {
            Lead lead = config.getHardwareLeads()[i];
            builder.append(", ").append(lead.name()).append(": ").append(Arrays.toString(mccData[i]));
        }

        return builder.toString();
    }

    public void fillData(IntBuffer buffer) {
        retainCounter.set(0);
        int marker = buffer.get();
        if (marker != PACKET_START_MARKER)
            throw new RuntimeException("Bad start marker: " + marker);

        chunkNum = buffer.get();
        lostValues = buffer.get();
        for (int i = 0; i < mccData.length; ++i) {
            buffer.get(mccData[i]);
        }
    }
}