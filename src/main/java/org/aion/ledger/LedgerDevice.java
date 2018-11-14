package org.aion.ledger;

import org.aion.ledger.exceptions.LedgerWriteException;
import purejavahidapi.HidDevice;
import purejavahidapi.InputReportListener;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import static org.aion.ledger.APDUWrapper.*;
import static org.aion.ledger.Constants.*;

public class LedgerDevice {

    private final HidDevice device;
    private byte[] buffer;
    private final ReentrantLock bufferLock = new ReentrantLock();

    public LedgerDevice(@Nonnull final HidDevice device) {
        this.device = device;
        this.buffer = new byte[0];
        setupDeviceListener();
    }

    private void setupDeviceListener() {
        this.device.setInputReportListener(new InputReportListener() {
            @Override
            public void onInputReport(HidDevice source, byte reportID, byte[] reportData, int reportLength) {
                bufferLock.lock();
                try {
                    // TODO: this might be better in a circular buffer
                    byte[] tempBuf = new byte[reportLength + buffer.length];
                    System.arraycopy(LedgerDevice.this.buffer, 0, tempBuf, 0, LedgerDevice.this.buffer.length);
                    System.arraycopy(reportData, 0, tempBuf, LedgerDevice.this.buffer.length, reportLength);
                    LedgerDevice.this.buffer = tempBuf;
                } finally {
                    bufferLock.unlock();
                }
            }
        });
    }

    // TODO: what if this is called twice?
    public void close() {
        this.device.close();
    }

    /**
     * Writes a chunk of 64-byte data out to the Ledger device.
     * As best as I can tell this should be always formatted in 64 byte
     * chunks.
     *
     * @implNote Note that there is no guarantee the data is actually written
     * to the device, or when this data is written to the device.
     *
     * @param arg 64-byte chunk of data to be written to device.
     */
    private void write(@Nonnull final byte[] arg) throws LedgerWriteException {
        assert arg.length == 64;
        int ret = this.device.setOutputReport((byte) 0x00, arg, arg.length);

        if (ret == -1) {
            throw new LedgerWriteException("failed to write to ledger device (-1)");
        }

        if (ret < 64) {
            // otherwise if ledger is just less than 64 bytes, try to write remaining
            int remaining = arg.length - ret;
            byte[] writeBuf = new byte[remaining];
            System.arraycopy(arg, ret, writeBuf, 0, remaining);
            ret = this.device.setOutputReport((byte) 0x00, writeBuf, writeBuf.length);
        }
    }

    /**
     * Retrieves a 64-byte chunk of data from the device
     * Note: this method should only be called from one thread
     *
     * @param waitPeriod how long to wait, <= 0 for indefinite blocking
     * @return {@code 64-byte} chunk of data, returns {@code null} on any conditions failing
     */
    private byte[] read(final int waitPeriod) {
        final long startTime = System.currentTimeMillis();
        while (true) {
            int bufferSize = 0;
            bufferLock.lock();
            try {
                bufferSize = this.buffer.length;
            } finally {
                bufferLock.unlock();
            }

            if (bufferSize >= 64) {
                break;
            }

            try {
                Thread.sleep(1);
                final long currentTime = System.currentTimeMillis();

                if (waitPeriod > 0 && (currentTime - startTime) > waitPeriod) {
                    return null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        // otherwise, return the 64-byte data chunk
        final byte[] outChunk = new byte[Constants.PACKET_SIZE];
        bufferLock.lock();
        final int chunkedBufferSize = this.buffer.length - Constants.PACKET_SIZE;
        final byte[] bufChunked = new byte[chunkedBufferSize];
        try {
            System.arraycopy(this.buffer, 0, outChunk, 0, Constants.PACKET_SIZE);
            System.arraycopy(this.buffer, Constants.PACKET_SIZE, bufChunked, 0, chunkedBufferSize);
            this.buffer = bufChunked;
        } finally {
            bufferLock.unlock();
        }
        return outChunk;
    }

    public byte[] exchange(@Nonnull final byte[] input) {
        assert input.length >= 5;
        assert (input.length - 5) == input[4];

        final byte[] wrappedInput = wrapCommandAPDU(CHANNEL, input, false);

        assert wrappedInput.length % PACKET_SIZE == 0;

        // TODO: or just plain ol byte arrays (?)
        final ByteBuffer buffer = ByteBuffer.wrap(wrappedInput);

        while (buffer.remaining() > PACKET_SIZE) {
            final byte[] packet = new byte[PACKET_SIZE];
            buffer.get(packet);
            try {
                write(packet);
            } catch (LedgerWriteException e) {
                throw new RuntimeException(e);
            }
        }

        // after writing is complete, starting reading from device
        // TODO
        return read(LEDGER_WAIT_TIMEOUT);
    }

    @Override
    public String toString() {
        return LedgerUtilities.deviceInfo(this.device.getHidDeviceInfo());
    }
}
