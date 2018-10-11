package ru.nordavind.ecgdonglelib.storage.mitbih;

import android.content.Context;
import android.media.MediaScannerConnection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ru.nordavind.ecgdonglelib.scan.DongleDataChunk;
import ru.nordavind.ecgdonglelib.storage.mitbih.signal.MitBihSignalWriter;

/**
 * Created by babay1 on 21.07.2015.
 */
public class MitBihJoinedSaver extends MitBihSaver {
    final ZipOutputStream outputStream;
    final MitBihSignalWriter signalWriter;
    boolean externalStream;

    public MitBihJoinedSaver(MitBihHeader header) throws IOException {
        super(header);

        outputStream = new ZipOutputStream(new FileOutputStream(header.filePath));
        ZipEntry datZipEntry = new ZipEntry(header.getChannelDataName(0));
        outputStream.putNextEntry(datZipEntry);
        signalWriter = makeWriter(header.formats[0], outputStream);
        externalStream = false;
    }

    public MitBihJoinedSaver(MitBihHeader header, ZipOutputStream zipStream) throws IOException {
        super(header);
        outputStream = zipStream;
        ZipEntry datZipEntry = new ZipEntry(header.getChannelDataName(0));
        zipStream.putNextEntry(datZipEntry);
        signalWriter = makeWriter(header.formats[0], outputStream);
        externalStream = true;
    }

    @Override
    public void write(List<DongleDataChunk> chunks) throws IOException {
        for (int i = 0; i < chunks.size(); i++) {
            writeChunk(chunks.get(i));
        }
    }

    @Override
    public void writeChunk(DongleDataChunk chunk) throws IOException {
        int[][] data = chunk.getRawData();
        int numberOfSamples = data[0].length;
        for (int i = 0; i < numberOfSamples; i++) {
            for (int channel = 0; channel < channels.length; ++channel) {
                int value = data[channel][i];
                signalWriter.addValue(value);
                header.crc[channel] += value;
            }
        }
        header.onValuesWritten(numberOfSamples);
        header.missingReadings += chunk.getBadValuesAmount();
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    void finishSignalWriter() throws IOException {
        signalWriter.finish();
        outputStream.closeEntry();
    }

    @Override
    public void close() throws IOException {
        finishSignalWriter();
        outputStream.flush();

        ZipEntry headerEntry = new ZipEntry(header.getHeaderShortName());
        outputStream.putNextEntry(headerEntry);
        header.writeTo(outputStream, true);
        outputStream.closeEntry();

        if (!externalStream) {
            outputStream.close();
        }
    }


    @Override
    public void scanFiles(Context context) {
        MediaScannerConnection.scanFile(context, new String[]{header.filePath}, null, null);
    }

}
