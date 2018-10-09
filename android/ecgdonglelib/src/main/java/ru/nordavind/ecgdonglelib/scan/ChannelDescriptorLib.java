package ru.nordavind.ecgdonglelib.scan;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

/**
 * Describes single hardware channel and sample values in a chunk
 */
public class ChannelDescriptorLib implements Serializable {

    /**
     * hardware channel number
     */
    public final int number;

    /**
     * sample size in bits
     */
    public final int sampleSize;

    /**
     * chunk size
     */
    public final int bufferSize;
    /**
     * see {@link SimpleCalibrationSettings#getZeroShift(int)}
     */
    public final int zeroShift;
    /**
     * samples period, ms
     */
    public final int period;

    /**
     * simple chunk duration =period * bufferSize
     */
    public final int bufferDuration;
    /**
     * samples frequency 1/s
     */
    public final double frequency;
    /**
     * max sample value
     */
    public final int maxValue;
    /**
     * min sample value
     */
    public final int minValue;
    /**
     * see {@link SimpleCalibrationSettings#getMul(int)}}
     */
    public final float floatMult;

    /**
     * see {@link SimpleCalibrationSettings#getMul(int)}}
     */
    public final double doubleMult;

    /**
     * calibration settings for device
     */
    @NonNull
    final SimpleCalibrationSettings calibrationSettings;

    public ChannelDescriptorLib(int number, @NonNull SimpleCalibrationSettings settings) {
        this.number = number;
        this.zeroShift = (int) settings.getZeroShift(number);
        this.bufferSize = settings.getValuesInReply();
        this.sampleSize = settings.getSampleSize();
        this.calibrationSettings = settings;
        this.frequency = settings.getDataFrequency();
        int p = (int) (1000 / frequency);
        this.period = p < 1 ? 1 : p;
        this.maxValue = settings.getMax();
        this.minValue = settings.getMin();
        this.bufferDuration = period * bufferSize;
        this.doubleMult = settings.getMul(number);
        this.floatMult = (float) doubleMult;

    }

    public static ChannelDescriptorLib[] createDescriptors(@NonNull SimpleCalibrationSettings settings) {
        int channels = settings.getChannelsCount();
        ChannelDescriptorLib[] descriptors = new ChannelDescriptorLib[channels];
        for (int i = 0; i < channels; i++) {
            descriptors[i] = new ChannelDescriptorLib(i, settings);
        }
        return descriptors;
    }

    public int getPeriod() {
        return period;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public float getAdcGain() {
        return (float) (1 / doubleMult);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getNumber() {
        return number;
    }

    public int getBufferDurationMs() {
        return bufferDuration;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "num: %d, bufSize : %d, period: %d", number, bufferSize, period);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelDescriptorLib)) return false;

        ChannelDescriptorLib that = (ChannelDescriptorLib) o;

        if (number != that.number) return false;
        if (sampleSize != that.sampleSize) return false;
        if (bufferSize != that.bufferSize) return false;
        if (zeroShift != that.zeroShift) return false;
        if (period != that.period) return false;
        if (bufferDuration != that.bufferDuration) return false;
        if (Double.compare(that.frequency, frequency) != 0) return false;
        if (maxValue != that.maxValue) return false;
        if (minValue != that.minValue) return false;
        if (Float.compare(that.floatMult, floatMult) != 0) return false;
        if (Double.compare(that.doubleMult, doubleMult) != 0) return false;
        return calibrationSettings.equals(that.calibrationSettings);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = number;
        result = 31 * result + sampleSize;
        result = 31 * result + bufferSize;
        result = 31 * result + zeroShift;
        result = 31 * result + period;
        result = 31 * result + bufferDuration;
        temp = Double.doubleToLongBits(frequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + maxValue;
        result = 31 * result + minValue;
        result = 31 * result + (floatMult != +0.0f ? Float.floatToIntBits(floatMult) : 0);
        temp = Double.doubleToLongBits(doubleMult);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + calibrationSettings.hashCode();
        return result;
    }

    public float getFloatMult() {
        return floatMult;
    }

    public double getDoubleMult() {
        return doubleMult;
    }

    public double getFrequency() {
        return frequency;
    }

    public int getZeroShift() {
        return zeroShift;
    }

    public float getMaxCalculatedValue() {
        return maxValue > minValue ? maxValue * floatMult : minValue * floatMult;
    }

    public float getMinCalculatedValue() {
        return minValue < maxValue ? minValue * floatMult : maxValue * floatMult;
    }

    /**
     * @return sample value that would be observed if the analog signal present at the ADC inputs
     * had a level that fell exactly in the middle of the input range of the ADC.
     */
    public int getAdcMiddle() {
        return 0;
    }

    /**
     * @return lead for this hardware channel
     */
    public Lead getLead() {
        return calibrationSettings.getChannelConfiguration().getHardwareLeads()[number];
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMinValue() {
        return minValue;
    }
}


