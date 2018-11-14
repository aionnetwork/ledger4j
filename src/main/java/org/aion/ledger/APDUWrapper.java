package org.aion.ledger;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.aion.ledger.Constants.PACKET_SIZE;

public class APDUWrapper {

    public static class SerializedPacket {
        public byte[] data;
        public int offset;

        public SerializedPacket(@Nonnull final byte[] serialized,
                                @Nonnull final int offset) {
            this.data = serialized;
            this.offset = offset;
        }
    }

    public static class DeserializedPacket {
        public byte[] data;

        // total response length of all command data
        // this will only be a non-zero value in the first packet
        public int totalResponseLength;

        public DeserializedPacket(@Nonnull final byte[] data,
                                  @Nonnull final int totalResponseLength) {
            this.data = data;
            this.totalResponseLength = totalResponseLength;
        }
    }

    public static class DeserializationException extends Exception {
        public DeserializationException(String reason) {
            super(reason);
        }
    }

    static SerializedPacket serializePacket(@Nonnull final int channel,
                                                   @Nonnull final byte[] command,
                                                   @Nonnull final int commandOffset,
                                                   @Nonnull final int sequenceIdx,
                                                   @Nonnull final boolean ble) {
        // assert some invariants
        // TODO: convert to proper runtime exceptions later
        assert channel > 0;
        assert channel <= (2 << 16);
        assert sequenceIdx <= (2 << 16);

        // allocate into 64 byte chunks
        final ByteBuffer buf = ByteBuffer.allocate(PACKET_SIZE);
        if (!ble) {
            buf.putShort((short) channel);
        }

        buf.put((byte) 0x05);
        buf.putShort((short) sequenceIdx);

        if (sequenceIdx == 0) {
            buf.putShort((short) command.length);
        }

        // remaining capacity is attributed to command data
        final int capacity = Math.min(buf.remaining(), command.length - commandOffset);
        final byte[] cmdArr = new byte[capacity];
        System.arraycopy(command, commandOffset, cmdArr, 0, capacity);
        buf.put(cmdArr);

        final byte[] bufArr = buf.array();
        return new SerializedPacket(bufArr, capacity);
    }

    public static byte[] wrapCommandAPDU(@Nonnull final int channel,
                                         @Nonnull final byte[] command,
                                         @Nonnull final boolean ble) {
        int commandOffset = 0;
        int sequenceIdx = 0;
        byte[] outBuf = new byte[0];

        while (command.length - commandOffset > 0) {
            SerializedPacket packet = serializePacket(channel, command, commandOffset, sequenceIdx, ble);

            // TODO: optimize later
            outBuf = ByteBuffer.allocate(outBuf.length + packet.data.length)
                        .put(outBuf).put(packet.data).array();

            commandOffset += packet.offset;
            sequenceIdx++;
        }
        return outBuf;
    }

    public static DeserializedPacket deserializePacket(@Nonnull final int channel,
                                                       @Nonnull final byte[] buffer,
                                                       @Nonnull final int sequenceIdx,
                                                       @Nonnull final boolean ble) throws DeserializationException {
        if ((sequenceIdx == 0 && buffer.length < 7) || (sequenceIdx > 0 && buffer.length < 5)) {
            // TODO: more meaningful exception
            throw new DeserializationException("Cannot deserialize packet, header information missing");
        }

        ByteBuffer buf = ByteBuffer.wrap(buffer);
        if (!ble) {
            final int dChannel = buf.getShort() & 0x0000FFFF;

            if (dChannel != channel) {
                throw new DeserializationException("Invalid channel");
            }
        }

        if (buf.get() != 0x05) {
            throw new DeserializationException("Invalid tag");
        }

        final int dSequenceIdx = buf.getShort() & 0x0000FFFF;
        if (dSequenceIdx != sequenceIdx) {
            throw new DeserializationException("Invalid sequenceIdx");
        }

        int totalResponseLength = 0;
        if (sequenceIdx == 0) {
            totalResponseLength = buf.getShort() & 0x0000FFFF;
        }

        final byte[] payload = new byte[buf.remaining()];
        buf.get(payload);
        return new DeserializedPacket(payload, totalResponseLength);
    }
}
