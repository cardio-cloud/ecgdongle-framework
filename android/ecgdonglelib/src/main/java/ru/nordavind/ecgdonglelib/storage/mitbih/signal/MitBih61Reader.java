package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.InputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay on 04.05.2016.
 */
public class MitBih61Reader extends MitBihSignalReader {
    long bytesRead;

    byte[] bytes = new byte[2];

    public MitBih61Reader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format61;
    }

    @Override
    public int readValue() throws IOException {
        int amount = 0;
        while (amount < 2) {
            if (amount < 0) {
                eof = true;
                return -1;
            }
            amount += inputStream.read(bytes, amount, bytes.length - amount);
        }
        bytesRead += amount;

        short value = (short) (((bytes[0] << 8) & 0xff00) | (bytes[1] & 0xff));
        return (int) value;
    }
}
