package org.aion.ledger;

import org.aion.ledger.exceptions.CommsException;
import org.aion.ledger.exceptions.LedgerWriteException;
import org.hid4java.HidServices;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import static org.aion.ledger.APDUWrapper.unwrapResponseAPDU;
import static org.aion.ledger.APDUWrapper.wrapCommandAPDU;
import static org.aion.ledger.ByteUtilities.merge;
import static org.aion.ledger.ByteUtilities.trimTail;
import static org.aion.ledger.Constants.CHANNEL;
import static org.aion.ledger.Constants.PACKET_SIZE;
import static org.aion.ledger.LedgerUtilities.bytesToHex;
import static org.aion.ledger.LedgerUtilities.hexToBytes;

public abstract class LedgerDevice {

    protected byte[] buffer;
    protected final ReentrantLock bufferLock = new ReentrantLock();

    // TODO: what if this is called twice?
    public abstract void close();

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
    protected abstract void write(@Nonnull final byte[] arg) throws LedgerWriteException;

    /**
     * Retrieves a 64-byte chunk of data from the device
     * Note: this method should only be called from one thread
     *
     * @param waitPeriod how long to wait, <= 0 for indefinite blocking
     * @return {@code 64-byte} chunk of data, returns {@code null} on any conditions failing
     */
    protected abstract byte[] read(final int waitPeriod);

    protected abstract void setNonBlocking(boolean cond);

    protected abstract void resetLedger();

    private static final byte[] SPECIAL_FAIL_ARR = hexToBytes("01010500BF0001040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

    @SuppressWarnings("Duplicates")
    public byte[] exchange(@Nonnull final byte[] input) throws CommsException {
        assert input.length >= 5;
        assert (input.length - 5) == input[4];

        final byte[] wrappedInput = wrapCommandAPDU(CHANNEL, input, false);

        assert wrappedInput.length % PACKET_SIZE == 0;

        // TODO: or just plain ol byte arrays (?)
        final ByteBuffer buffer = ByteBuffer.wrap(wrappedInput);

        while (buffer.remaining() >= PACKET_SIZE) {
            // TODO:
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
        byte[] ledgerResponse = new byte[0];
        byte[] deserialized = null;

        int failArrCounter = 0;
        LOOP:
        while (true) {
            byte[] respPacket = read(1000);
            ledgerResponse = merge(ledgerResponse, respPacket);

            try {
                deserialized = unwrapResponseAPDU(CHANNEL, ledgerResponse, false);
            } catch (APDUWrapper.DeserializationException e) {

                // handles a special case that can occur on MacOS in which
                // the handle (path) for the ledger is improperly published
                // leading us to write to the wrong HID interface TODO: confirm
                // in such a situation, request a reset of the ledger device
                // and try writing to the buffer again
                if (Arrays.equals(respPacket, SPECIAL_FAIL_ARR)) {
                    resetLedger();

                    // rewind and prepare for rewrite
                    buffer.rewind();
                    // reset the current response buffers, since messages are corrupt
                    ledgerResponse = new byte[0];
                    while (buffer.remaining() >= PACKET_SIZE) {
                        // TODO:
                        final byte[] packet = new byte[PACKET_SIZE];
                        buffer.get(packet);
                        try {
                            write(packet);
                        } catch (LedgerWriteException e2) {
                            throw new RuntimeException(e2);
                        }
                    }
                    failArrCounter++;

                    // based on the assumption that this may be time related
                    if (failArrCounter >= 6) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(ie);
                        }
                    }

                    if (failArrCounter >= 10) {
                        throw new CommsException("triggered wrongly published path too many times (10), HID information: " + this.toString());
                    }

                    continue LOOP;
                }

                // this indicates there was an unrecoverable issue with deserialization
                // best to wrap in an LedgerException and throw
                throw new CommsException("deserialization error, cannot proceed", e);
            }

            // indicates a successful deserialization
            if (deserialized != null) {
                break;
            }
        }

        // interpret results of deserialization
        final int swOffset = deserialized.length - 2;
        final int sw = (deserialized[swOffset] << 8 + deserialized[swOffset + 1]) & 0xFFFF;
        switch(sw) {
            case 0x9000:
                return trimTail(deserialized, 2);
            case 0x6982:
                throw new CommsException(sw, "Have you installed the existing CA with resetCustomCA first?");
            case 0x6985:
                throw new CommsException(sw, "Condition of use not satisifed (denied by user?");
            case 0x6a84:
            case 0x6a85:
                throw new CommsException(sw, "Not enough space?");
            case 0x6484:
                throw new CommsException(sw, "Are you using the correct targetId?");
            default:
                throw new CommsException(sw, "Unknown reason");
        }
    }
}
