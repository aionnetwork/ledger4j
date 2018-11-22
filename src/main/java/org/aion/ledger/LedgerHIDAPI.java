package org.aion.ledger;

import org.aion.ledger.exceptions.LedgerWriteException;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;

import static org.aion.ledger.Constants.PACKET_SIZE;

public class LedgerHIDAPI extends LedgerDevice {

    private HidDevice device;

    public LedgerHIDAPI(@Nonnull final HidDevice device) {
        this.device = device;
    }

    // TODO: what if this is called twice?
    @Override
    public void close() {
        if (this.device.isOpen()) {
            this.device.close();
        }
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
    @Override
    protected void write(@Nonnull final byte[] arg) throws LedgerWriteException {
        this.device.write(arg, arg.length, (byte) 0x00);
    }

    /**
     * Retrieves a 64-byte chunk of data from the device
     * Note: this method should only be called from one thread
     *
     * @param waitPeriod how long to wait, <= 0 for indefinite blocking
     * @return {@code 64-byte} chunk of data, returns {@code null} on any conditions failing
     */
    @Nullable
    @Override
    protected byte[] read(final int waitPeriod) {
        byte[] data = new byte[PACKET_SIZE];
        int resp = this.device.read(data);

        if (resp < 0) {
            // TODO: should distinguish this state
            return null;
        }
        return data;
    }

    @Override
    protected void resetLedger() {
        this.device.close();
        HidServices services = HidManager.getHidServices();
        services.scan();
        try {
            // TODO: violates abstraction
            this.device = ((LedgerHIDAPI) LedgerUtilities.findLedgerDevice()).device;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setNonBlocking(boolean condition) {
        this.device.setNonBlocking(condition);
    }

    @Override
    public String toString() {
        return this.device.toString();
    }
}
