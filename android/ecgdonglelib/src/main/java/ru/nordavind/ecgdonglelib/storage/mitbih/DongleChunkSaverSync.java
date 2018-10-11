package ru.nordavind.ecgdonglelib.storage.mitbih;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;
import ru.nordavind.ecgdonglelib.storage.IChunkSaver;


/**
 * Created by babay1 on 11.08.2015.
 */
public class DongleChunkSaverSync implements IChunkSaver {

    private final ScanConfig config;
    private final File outputFile;
    private final int firstChunkNum;
    private final Context context;
    private MitBihJoinedSaver saver;

    /**
     * @param context    -- might be null. There is an issue in Android.
     *                   If you create a file and then attach your android device to PC, the file
     *                   will not be visible on PC until you reboot your device;
     *                   Context is used solve that issue
     * @param firstChunk first chunk to save;
     * @param filePath   file path to save to
     */
    public DongleChunkSaverSync(Context context, DongleDataChunk firstChunk, String filePath) throws IOException {
        this.context = context == null ? null : context.getApplicationContext();
        this.config = firstChunk.config;
        outputFile = new File(filePath);
        firstChunkNum = firstChunk.getChunkNum();

        MitBihHeader header = MitBihHeader.make(outputFile, config, MitBihFormat.Format16, System.currentTimeMillis());
        saver = new MitBihJoinedSaver(header);
    }

    /**
     * @param firstChunk first chunk to save;
     * @param filePath   file path to save to
     */
    public DongleChunkSaverSync(DongleDataChunk firstChunk, String filePath) throws IOException {
        this(null, firstChunk, filePath);
    }


    @Override
    public void addChunk(@NonNull final DongleDataChunk chunk) throws IOException {
        if (saver != null && chunk.getChunkNum() >= firstChunkNum) {
            saver.writeChunk(chunk);
        }
    }

    @Override
    public void addChunks(@NonNull final List<DongleDataChunk> chunks) throws IOException {
        if (saver != null)
            saver.write(chunks);
    }

    @Override
    public void close() throws IOException {
        if (saver != null) {
            saver.close();
            if (context != null)
                saver.scanFiles(context);
            saver = null;
        }
    }
}
