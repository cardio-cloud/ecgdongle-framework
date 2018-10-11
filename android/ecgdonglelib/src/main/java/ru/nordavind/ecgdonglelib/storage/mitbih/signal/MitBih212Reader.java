package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.InputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by Babay on 22.12.2014.
 */
public class MitBih212Reader extends MitBihSignalReader {
    int state = 0;
    byte[] bytes = new byte[3];

    public MitBih212Reader(int channelN, InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format212;
    }

    @Override
    public int readValue() throws IOException {
        int value;
        int totalBytesRead = 0;
        int bytesRead;
        if (state == 0) {
            while (totalBytesRead < 3) {
                bytesRead = inputStream.read(bytes, totalBytesRead, 3 - totalBytesRead);
                if (bytesRead == -1 || bytesRead == 0) {
                    eof = true;
                    return -1;
                }
                totalBytesRead += bytesRead;
            }

            value = (byte) ((bytes[1] & 0x0f) << 4);
            value = value << 4;
            //byte significant = (byte) ((bytes[1] & 0x0f) <<4);
            //value |= significant << 4;
            //value |= (byte)((bytes[1] & 0x0f) << 8);
            value |= bytes[0] & 0xff; // read least significant byte;
            state = 1;
            return value;
        } else {
            value = (byte) ((bytes[1] & 0xf0)) << 4;
            value |= bytes[2] & 0xff;
            state = 0;
            return value;
        }
    }
}
