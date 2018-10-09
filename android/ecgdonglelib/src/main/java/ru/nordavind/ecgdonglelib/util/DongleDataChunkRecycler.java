package ru.nordavind.ecgdonglelib.util;


import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.scan.ScanConfig;

/**
 * used to recycle data chunks see {@link IRecycleable} for details
 */
public class DongleDataChunkRecycler extends ObjectRecycler<DongleDataChunk> {
    private final ScanConfig config;

    public DongleDataChunkRecycler(ScanConfig config) {
        super(20);
        this.config = config;
    }

    @Override
    protected DongleDataChunk createNew() {
        return new DongleDataChunk(config, this);
    }
}
