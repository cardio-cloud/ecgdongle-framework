package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.OutputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay on 04.05.2016.
 */
public class MitBih16Writer extends MitBihSignalWriter {

    public static final int EOF = 0x8000;

    private final byte[] bytes = new byte[2];

    public MitBih16Writer(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    protected void storeValue(int value) throws IOException {
        bytes[0] = (byte) (value & 0xFF);
        bytes[1] = (byte) ((value & 0xFF00) >> 8);
        outputStream.write(bytes);
    }

    @Override
    public void finish() throws IOException {
        storeValue(EOF);
        storeValue(0);
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format16;
    }
}
