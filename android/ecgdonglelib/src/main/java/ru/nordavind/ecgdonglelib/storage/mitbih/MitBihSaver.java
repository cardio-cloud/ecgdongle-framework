package ru.nordavind.ecgdonglelib.storage.mitbih;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import ru.nordavind.ecgdonglelib.scan.ChannelDescriptorLib;
import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.storage.mitbih.signal.MitBih16Writer;
import ru.nordavind.ecgdonglelib.storage.mitbih.signal.MitBih212Writer;
import ru.nordavind.ecgdonglelib.storage.mitbih.signal.MitBih61Writer;
import ru.nordavind.ecgdonglelib.storage.mitbih.signal.MitBihSignalWriter;

/**
 * Created by Babay on 18.12.2014.
 */
public abstract class MitBihSaver {
    final protected MitBihHeader header;
    final protected ChannelDescriptorLib[] channels;

    public MitBihSaver(MitBihHeader header) {
        this.header = header;
        this.channels = header.channels;
        if (channels == null) {
            try {
                throw new IllegalStateException("channels is null + " + header.scanConfig.toJson().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected static MitBihSignalWriter makeWriter(MitBihFormat format, OutputStream outputStream) {
        switch (format) {
            case Format212:
                return new MitBih212Writer(outputStream);

            case Format61:
                return new MitBih61Writer(outputStream);

            case Format16:
                return new MitBih16Writer(outputStream);

            case Format212unsigned:
                throw new UnsupportedOperationException("Should non use Format212unsigned format");

            default:
                throw new UnsupportedOperationException("format not supported:" + format.name());
        }
    }

    public MitBihHeader getHeader() {
        return header;
    }

    public abstract void write(List<DongleDataChunk> chunks) throws IOException;

    public abstract void writeChunk(DongleDataChunk chunk) throws IOException;

    public abstract void flush() throws IOException;

    public abstract void close() throws IOException;

    public abstract void scanFiles(Context context);

    protected synchronized void writeHeader(File file, boolean withJsonConfig) {
        try {
            FileOutputStream stream = new FileOutputStream(file, false);
            header.writeTo(stream, withJsonConfig);
            stream.close();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

}
