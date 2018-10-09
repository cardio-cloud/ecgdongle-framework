package ru.nordavind.ecgdonglelib;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import ru.nordavind.ecgdonglelib.filter.ScanFilterInfo;
import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;
import ru.nordavind.ecgdonglelib.util.DongleDataChunkRecycler;

import static ru.nordavind.ecgdonglelib.Settings.TAG;

/**
 * Reads chunks from socket
 * Used to receive chunk data from Service
 */

public class DataReplyReader {

    private final ScanConfig scanConfig;
    private final LocalSocket socket;
    private final Thread thread;
    private final IECGDongleServiceWrapper.OnNextDataReplyListener listener;
    private final DongleDataChunkRecycler chunkRecycler;
    private final IntBuffer dataReplyBufferInt;
    private final ByteBuffer dataReplyBuffer;
    private final byte[] dataReplyBufferBody;
    private volatile boolean doRun = true;
    private boolean stopped;

    public DataReplyReader(ScanConfig scanConfig, IECGDongleServiceWrapper.OnNextDataReplyListener listener) {
        this.scanConfig = scanConfig;
        this.listener = listener;
        this.chunkRecycler = new DongleDataChunkRecycler(scanConfig);

        dataReplyBufferBody = new byte[scanConfig.getBufferSizeForDataReply()];
        dataReplyBuffer = ByteBuffer.wrap(dataReplyBufferBody).order(ByteOrder.nativeOrder());
        dataReplyBufferInt = dataReplyBuffer.asIntBuffer();

        socket = new LocalSocket();

        thread = new Thread(this::threadBody, "DataReplyReader thread");
        thread.start();
    }

    private void threadBody() {
        InputStream socketInputStream;
        try {
            socket.connect(new LocalSocketAddress(scanConfig.socketName));
            socket.setSoTimeout(200);
            socketInputStream = socket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopped = false;
        dataReplyBuffer.position(0);
        int position = 0;
        while (doRun) {
            try {
                int maxLen = dataReplyBufferBody.length - position;
                int amount = socketInputStream.read(dataReplyBufferBody, position, maxLen);
                if (amount == -1) {
                    doRun = false;
                    continue;
                }
                position += amount;

                if (position < dataReplyBufferBody.length)
                    continue;

                DongleDataChunk dataReply = chunkRecycler.obtain(true);
                dataReplyBufferInt.position(0);
                dataReply.fillData(dataReplyBufferInt);
                position = 0;

                listener.onNextDataReply(dataReply);
            } catch (NullPointerException e) {
                Log.e(TAG, "threadBody: ", e);
                doRun = false;
            } catch (Exception e) {
                if (!"Try again".equals(e.getMessage()))
                    Log.e(TAG, "threadBody: ", e);
            }

        }
        try {
            Log.d(TAG, "DataReplyReader: Socket close");
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "threadBody: ", e);
        }
        stopped = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void stop() {
        doRun = false;
        thread.interrupt();
        synchronized (this) {
            while (!stopped) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public ScanConfig onFiltersChanged(ScanFilterInfo filter) {
        scanConfig.setFilter(filter);
        return scanConfig;
    }
}
