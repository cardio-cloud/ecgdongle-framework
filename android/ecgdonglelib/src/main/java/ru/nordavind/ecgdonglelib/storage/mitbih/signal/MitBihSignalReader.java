package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.InputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by Babay on 22.12.2014.
 */
public abstract class MitBihSignalReader {
    protected final InputStream inputStream;
    /**
     * flag set when we've reached EOF
     */
    protected boolean eof;

    public MitBihSignalReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public int readValues(int[] buffer) throws IOException {
        if (eof)
            return -1;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = readValue();
            if (eof) {
                return i;
            }
        }

        return buffer.length;
    }

    public InputStream getStream() {
        return inputStream;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public abstract MitBihFormat getFormat();

    /**
     * reads next value;
     * sets eof flag if we've hit EOF.
     *
     * @return read value; result is undefined if EOF flag is set;
     * @throws IOException
     */
    public abstract int readValue() throws IOException;
}
