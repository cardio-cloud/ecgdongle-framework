package ru.nordavind.ecgdonglelib.storage.mitbih;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;
import ru.nordavind.ecgdonglelib.storage.IChunkSaver;

import static ru.nordavind.ecgdonglelib.Settings.TAG;


/**
 * Created by babay.
 */
public class DongleChunkSaverAsync implements IChunkSaver {
    private static int counter = 0;

    private final ScanConfig config;
    private final HandlerThread fileThread;
    private final Handler handler;
    private final File outputFile;
    private final int firstChunkNum;
    private final Context context;
    private final ExceptionListener exceptionListener;
    private MitBihJoinedSaver saver;

    /**
     * @param context           -- might be null. There is an issue in Android.
     *                          If you create a file and then attach your android device to PC, the file
     *                          will not be visible on PC until you reboot your device;
     *                          Context is used solve that issue
     * @param firstChunk        first chunk to save;
     * @param filePath          file path to save to
     * @param exceptionListener callback to be called when exception happens;
     */
    public DongleChunkSaverAsync(Context context, final DongleDataChunk firstChunk, final String filePath, @Nullable ExceptionListener exceptionListener) throws IOException {
        this.context = context == null ? null : context.getApplicationContext();
        this.config = firstChunk.config;
        outputFile = new File(filePath);
        firstChunkNum = firstChunk.getChunkNum();
        this.exceptionListener = exceptionListener;

        fileThread = new HandlerThread(String.format(Locale.US, "dongle chunk saver thread #%d", ++counter));
        fileThread.start();
        handler = new Handler(fileThread.getLooper());

        MitBihHeader header = MitBihHeader.make(outputFile, config, MitBihFormat.Format16, System.currentTimeMillis());
        saver = new MitBihJoinedSaver(header);
    }

    /**
     * @param firstChunk        first chunk to save;
     * @param filePath          file path to save to
     * @param exceptionListener callback to be called when exception happens;
     */
    public DongleChunkSaverAsync(final DongleDataChunk firstChunk, final String filePath, @Nullable ExceptionListener exceptionListener) throws IOException {
        this(null, firstChunk, filePath, exceptionListener);
    }

    @Override
    public void addChunk(@NonNull final DongleDataChunk chunk) {
        chunk.retain();

        handler.post(() -> {
            try {
                if (saver != null && chunk.getChunkNum() >= firstChunkNum) {
                    saver.writeChunk(chunk);
                }
                chunk.release();
            } catch (IOException e) {
                onException(e);
            }
        });
    }

    @Override
    public void addChunks(@NonNull final List<DongleDataChunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).retain();
        }

        handler.post(() -> {
            if (saver != null) {
                try {
                    saver.write(chunks);
                } catch (IOException e) {
                    onException(e);
                }
            }
            for (int i = 0; i < chunks.size(); i++) {
                chunks.get(i).release();
            }
        });
    }

    @Override
    public void close() {
        handler.post(() -> {
            try {
                if (saver != null) {
                    saver.close();
                    if (context != null)
                        saver.scanFiles(context);
                    saver = null;
                }

                fileThread.quit();
            } catch (IOException e) {
                onException(e);
            }
        });
    }

    private void onException(IOException e) {
        Log.e(TAG, e.getMessage(), e);
        if (exceptionListener != null)
            new Handler(Looper.getMainLooper()).post(() -> exceptionListener.onIoException(e));
    }

    public interface ExceptionListener {
        void onIoException(IOException e);
    }
}
