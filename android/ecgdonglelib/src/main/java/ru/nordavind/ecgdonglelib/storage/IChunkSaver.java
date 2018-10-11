package ru.nordavind.ecgdonglelib.storage;

import android.support.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;

public interface IChunkSaver extends Closeable {

    /**
     * writes chunk to file
     *
     * @param chunk to write
     */
    void addChunk(@NonNull final DongleDataChunk chunk) throws IOException;

    /**
     * writes some chunks to file
     *
     * @param chunks chunks to write
     */
    void addChunks(@NonNull final List<DongleDataChunk> chunks) throws IOException;
}
