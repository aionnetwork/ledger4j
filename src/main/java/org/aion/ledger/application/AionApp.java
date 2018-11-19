package org.aion.ledger.application;

import org.aion.ledger.KeyAddress;
import org.aion.ledger.LedgerDevice;
import org.aion.ledger.LedgerUtilities;
import org.aion.ledger.exceptions.CommsException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static org.aion.ledger.LedgerUtilities.toHardenedOffset;

public class AionApp {

    private final LedgerDevice ledgerDevice;

    /**
     * Aion specific functionality, configures the ledger to correspond
     * to the Ledger AION application
     *
     * @param ledgerDevice underlying ledger device
     */
    public AionApp(LedgerDevice ledgerDevice) {
        this.ledgerDevice = ledgerDevice;
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
    static byte[] generateBip32Path(final int offset) {
        final long offsetExpanded = offset & 0x00000000FFFFFFFFL;

        if (offsetExpanded > BIP44_MAX_OFFSET) {
            throw new IllegalArgumentException("offset cannot be greater than BIP44_MAX_OFFSET=" + BIP44_MAX_OFFSET);
        }

        final byte[] bip44FullPath = new byte[20];
        System.arraycopy(BIP44_PATH, 0, bip44FullPath, 0, BIP44_PATH.length);
        final byte[] offsetBytes = toHardenedOffset(offset);
        System.arraycopy(offsetBytes, 0, bip44FullPath, BIP44_PATH.length, offsetBytes.length);
        return bip44FullPath;
    }

    @Nonnull
    static byte[] publicKeyAPDUCommand(@Nonnull final byte[] bip32Path) {
        ByteBuffer buf = ByteBuffer.allocate(AION_APP_PREFIX_SIZE +
                INS_CMD_SIZE + HEADER_CMD_PADDING_SIZE + HEADER_PAYLOAD_SIZE + bip32Path.length);
        buf.put((byte) AION_APP_PREFIX);
        buf.put(INS_GET_PUBLIC_KEY);
        buf.put(HEADER_CMD_PADDING);
        buf.put((byte) (bip32Path.length + 1));
        buf.put((byte) (bip32Path.length / 4));
        buf.put(bip32Path);
        return buf.array();
    }

    @Nonnull
    static byte[] signPayloadAPDUCommand(@Nonnull final byte[] bip32Path, @Nonnull final byte[] payload) {
        final int payloadLength = bip32Path.length + payload.length;
        ByteBuffer buf = ByteBuffer.allocate(AION_APP_PREFIX_SIZE +
                INS_CMD_SIZE + HEADER_CMD_PADDING_SIZE + HEADER_PAYLOAD_SIZE + bip32Path.length + payload.length);
        buf.put((byte) AION_APP_PREFIX);
        buf.put(INS_SIGN);
        buf.put(HEADER_CMD_PADDING);

        // TODO: figure out this mystery
        buf.put((byte) (payloadLength + 1));
        buf.put((byte) (bip32Path.length / 4));
        buf.put(bip32Path);
        buf.put(payload);
        return buf.array();
    }

    /**
     * Retrieves the public key of the connected ledger AION app, given the
     * <b>offset</b> from the HD path.
     *
     * @apiNote notice here that the range of the offset parameter is:
     * 0 <= offset <= 0x0FFFFFFF
     *
     * There _is_ an assertion check in place to guarantee this invariant
     * TODO: should probably be moved to a runtime exception
     *
     * @param offset of the given address
     * @return the public key and address returned from the ledger AION app
     */
    @Nullable
    public KeyAddress getPublicKey(final int offset) throws CommsException {
        byte[] bip32Path = generateBip32Path(offset);
        byte[] pkApdu = publicKeyAPDUCommand(bip32Path);
        byte[] out = ledgerDevice.exchange(pkApdu);

        if (out.length != 64) {
            throw new CommsException("invalid length");
        }

        // output packed with first 32-bytes PK, latter 32-bytes address
        byte[] pubKey = new byte[32];
        byte[] address = new byte[32];

        System.arraycopy(out, 0, pubKey, 0, 32);
        System.arraycopy(out, 32, address, 0, 32);
        return new KeyAddress(pubKey, address);
    }

    /**
     * Given an index offset (indicating which account to sign from in HD Path) and
     * a payload to sign, returns the signature
     *
     * @param offset of the account
     * @param payload message to be signed
     * @return 64-byte signature of the payload
     * @throws CommsException
     */
    @Nullable
    public byte[] signPayload(final int offset, @Nonnull final byte[] payload) throws CommsException {

        if (payload.length > 0xFF) {
            throw new IllegalArgumentException("payload cannot be greater than 255 (0xFF) bytes");
        }

        byte[] bip32Path = generateBip32Path(offset);
        byte[] apduCmd = signPayloadAPDUCommand(bip32Path, payload);
        return ledgerDevice.exchange(apduCmd);
    }
}
