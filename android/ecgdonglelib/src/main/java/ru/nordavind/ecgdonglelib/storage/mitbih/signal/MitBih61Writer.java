package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.OutputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay on 04.05.2016.
 */
public class MitBih61Writer extends MitBihSignalWriter {

    private final byte[] bytes = new byte[2];

    public MitBih61Writer(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    protected void storeValue(int value) throws IOException {
        bytes[0] = (byte) ((value & 0xFF00) >> 8);
        bytes[1] = (byte) (value & 0xFF);
        outputStream.write(bytes);
    }

    @Override
    public void finish() throws IOException {
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format61;
    }
}
