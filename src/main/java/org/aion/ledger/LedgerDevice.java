package org.aion.ledger;

import purejavahidapi.HidDevice;
import purejavahidapi.InputReportListener;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReentrantLock;

public class LedgerDevice {

    private final HidDevice device;
    private byte[] buffer;
    private final ReentrantLock bufferLock = new ReentrantLock();

    public LedgerDevice(@Nonnull HidDevice device) {
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

    public void close() {
        this.device.close();
    }

    public void write(@Nonnull byte[] arg) {
        this.device.setOutputReport();
    }


    // TODO: this does ugly weird spin locking
    public byte[] read(int waitPeriod) {
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    public void readError() {

    }

    @Override
    public String toString() {
        return LedgerUtilities.deviceInfo(this.device.getHidDeviceInfo());
    }
}
