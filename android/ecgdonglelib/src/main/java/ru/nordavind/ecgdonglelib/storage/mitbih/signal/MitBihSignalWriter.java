package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.OutputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay on 23.10.2014.
 */
public abstract class MitBihSignalWriter {

    /**
     * stream to write to
     */
    protected final OutputStream outputStream;
    /**
     * amount of written values
     */
    protected int amount = 0;
    /**
     * crc of written values
     */
    protected int crc = 0;

    public MitBihSignalWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void addValue(int value) throws IOException {
        crc += value;
        storeValue(value);
        amount++;
    }

    public void write(int[] buffer, int start, int amount) throws IOException {
        for (int i = start, end = start + amount; i < end; i++) {
            int value = buffer[i];
            crc += value;
            storeValue(value);
        }
        this.amount += amount;
    }

    public void write(int[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    public int getTotalWrittenValues() {
        return amount;
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public int getCRC() {
        return crc;
    }

    public abstract void finish() throws IOException;

    public abstract MitBihFormat getFormat();

    protected abstract void storeValue(int value) throws IOException;
}
