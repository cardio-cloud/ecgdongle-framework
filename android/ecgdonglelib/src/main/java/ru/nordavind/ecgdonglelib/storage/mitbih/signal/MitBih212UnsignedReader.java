package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.InputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by Babay on 22.12.2014.
 */
public class MitBih212UnsignedReader extends MitBihSignalReader {

    final int zeroShift;
    int state = 0;
    byte[] bytes = new byte[3];

    public MitBih212UnsignedReader(int channelN, InputStream inputStream, int zeroShift) {
        super(inputStream);
        this.zeroShift = zeroShift;
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format212unsigned;
    }

    @Override
    public int readValue() throws IOException {
        int value;

        if (state == 0) {
            if (inputStream.read(bytes, 0, 3) != 3) {
                eof = true;
                return -1;
            }
            value = bytes[0] & 0xff; // read least significant byte;
            value |= (bytes[1] & 0x0f) << 8;
            state = 1;
            return value;
        } else {
            value = (bytes[1] & 0xf0) << 4;
            value |= bytes[2] & 0xff;
            state = 0;
            return value;
        }
    }
}
