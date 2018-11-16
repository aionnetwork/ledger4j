package org.aion.ledger;

import org.aion.ledger.exceptions.LedgerWriteException;
import purejavahidapi.HidDevice;
import purejavahidapi.InputReportListener;

import javax.annotation.Nonnull;

import static org.aion.ledger.Constants.PACKET_SIZE;

public class LedgerPJHID extends LedgerDevice {

    private final HidDevice device;

    public LedgerPJHID(@Nonnull final HidDevice device) {
        this.device = device;
        this.buffer = new byte[0];
        setupDeviceListener();
    }

    private void setupDeviceListener() {
        this.device.setInputReportListener(new InputReportListener() {
            @Override
            public void onInputReport(HidDevice source, byte reportID, byte[] reportData, int reportLength) {
                // TODO: it seems we're getting garbage data back
                bufferLock.lock();
                try {
                    // TODO: this might be better in a circular buffer
                    byte[] tempBuf = new byte[reportLength + buffer.length];
                    System.arraycopy(LedgerPJHID.this.buffer, 0, tempBuf, 0, LedgerPJHID.this.buffer.length);
                    System.arraycopy(reportData, 0, tempBuf, LedgerPJHID.this.buffer.length, reportLength);
                    LedgerPJHID.this.buffer = tempBuf;
                } finally {
                    bufferLock.unlock();
                }
            }
        });
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected void write(@Nonnull final byte[] arg) throws LedgerWriteException {
        // assert arg.length == 64;
        // TODO: the ledgerblue implementation manually attaches at 0x0 in front
        // this is for HID api's channel when setting an output report
        // we already have this feature included by library
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
    @Override
    protected byte[] read(final int waitPeriod) {
        final long startTime = System.currentTimeMillis();
        while (true) {
            int bufferSize = 0;
            bufferLock.lock();
            try {
                bufferSize = this.buffer.length;
            } finally {
                bufferLock.unlock();
            }

            if (bufferSize >= PACKET_SIZE) {
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

    @Override
    public void close() {
        this.device.close();
    }

    @Override
    public String toString() {
        return LedgerUtilities.deviceInfo(this.device.getHidDeviceInfo());
    }
}
