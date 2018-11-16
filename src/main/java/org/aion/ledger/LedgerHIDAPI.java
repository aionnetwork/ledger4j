package org.aion.ledger;

import org.aion.ledger.exceptions.LedgerWriteException;
import org.hid4java.HidDevice;

import javax.annotation.Nonnull;

public class LedgerHIDAPI extends LedgerDevice {

    private final HidDevice device;

    public LedgerHIDAPI(HidDevice device) {
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
        // TODO
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
        // TODO
        return null;
    }
}
