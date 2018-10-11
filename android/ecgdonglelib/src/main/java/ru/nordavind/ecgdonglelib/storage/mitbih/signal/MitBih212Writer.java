package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.OutputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay on 23.10.2014.
 * <p/>
 * less math - can use this for internal storages
 */
public class MitBih212Writer extends MitBihSignalWriter {
    int state = 0;
    byte[] bytes = new byte[3];

    public MitBih212Writer(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format212;
    }

    @Override
    protected void storeValue(int value) throws IOException {
        switch (state++) {
            case 0:
                bytes[0] = (byte) (value & 0xff);
                bytes[1] = (byte) ((value >> 8) & 0x0f);
                break;
            case 1:
                bytes[1] |= ((value >> 4) & 0xf0);
                bytes[2] = (byte) (value & 0xff);
                outputStream.write(bytes);
                state = 0;
                break;
        }
    }

    @Override
    public void finish() throws IOException {
        if (state != 0)
            addValue(0);
    }
}
