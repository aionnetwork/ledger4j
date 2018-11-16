package org.aion.ledger;

import org.aion.ledger.exceptions.LedgerWriteException;
import purejavahidapi.HidDevice;
import purejavahidapi.InputReportListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import static org.aion.ledger.APDUWrapper.wrapCommandAPDU;
import static org.aion.ledger.Constants.*;
import static org.aion.ledger.LedgerUtilities.toHardenedOffset;

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

    public byte[] exchange(@Nonnull final byte[] input) {
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
        return read(LEDGER_WAIT_TIMEOUT);
    }

    // AION specific functionality

    // this is (most likely?) specific to Ledger's AION Application
    private static final byte   INS_GET_PUBLIC_KEY = 0x02;
    private static final byte   INS_SIGN = 0x04;
    private static final byte   INS_GET_APP_CONFIGURATION = 0x06;
    private static final byte   INS_SIGN_PERSONAL_MESSAGE = 0x08;
    private static final int    INS_CMD_SIZE = 1;

    private static final int    AION_APP_PREFIX = 0xe0;
    private static final int    AION_APP_PREFIX_SIZE = 1;

    private static final byte[] HEADER_CMD_PADDING = new byte[] {(byte) 0x00, (byte) 0x00};
    private static final int    HEADER_CMD_PADDING_SIZE = 2;
    private static final int    HEADER_PAYLOAD_SIZE = 2;

    // BIP44 specific path
    // 44'/425'/0/0
    private static final byte[] BIP44_PATH = LedgerUtilities.hexToBytes("8000002C800001A98000000080000000");
    private static final long BIP44_MAX_OFFSET = 0x0FFFFFFFL;

    /**
     * Generates a BIP44 path with the assumption that the offset will also be
     * a hardened parameter.
     *
     * @param offset integer representing the offset of the index
     * @return byte array representing the full bip44 path (20 bytes)
     */
    @Nonnull
    protected static byte[] generateBip32Path(final int offset) {
        final long offsetExpanded = offset & 0x00000000FFFFFFFFL;
        assert offsetExpanded <= BIP44_MAX_OFFSET;

        final byte[] bip44FullPath = new byte[20];
        System.arraycopy(BIP44_PATH, 0, bip44FullPath, 0, BIP44_PATH.length);
        final byte[] offsetBytes = toHardenedOffset(offset);
        System.arraycopy(offsetBytes, 0, bip44FullPath, BIP44_PATH.length, offsetBytes.length);
        return bip44FullPath;
    }

    @Nonnull
    protected static byte[] publicKeyAPDUCommand(@Nonnull final byte[] bip32Path) {
        ByteBuffer buf = ByteBuffer.allocate(AION_APP_PREFIX_SIZE + INS_CMD_SIZE + HEADER_CMD_PADDING_SIZE + HEADER_PAYLOAD_SIZE + bip32Path.length);
        buf.put((byte) AION_APP_PREFIX);
        buf.put((byte) INS_GET_PUBLIC_KEY);
        buf.put(HEADER_CMD_PADDING);
        buf.put((byte) (bip32Path.length + 1));
        buf.put((byte) (bip32Path.length / 4));
        buf.put(bip32Path);
        return buf.array();
    }

    @Nullable
    public byte[] getPublicKey(final int offset) {
        byte[] bip32Path = generateBip32Path(offset);
        byte[] pkApdu = publicKeyAPDUCommand(bip32Path);
        return exchange(pkApdu);
    }
}
