package ru.nordavind.ecgdonglelib.storage.mitbih.signal;

import java.io.IOException;
import java.io.InputStream;

import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay on 04.05.2016.
 */
public class MitBih16Reader extends MitBihSignalReader {

    long bytesRead;

    byte[] bytes = new byte[2];

    public MitBih16Reader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public MitBihFormat getFormat() {
        return MitBihFormat.Format16;
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

        short valueS = (short) (((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff));
        int value = (int) valueS;
        if (value == MitBih16Writer.EOF && inputStream.available() <= 2) {
            eof = true;
            return -1;
        }
        return value;
    }
}
