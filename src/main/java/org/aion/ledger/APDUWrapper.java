package org.aion.ledger;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public class APDUWrapper {

    public static final int PACKET_SIZE = 64;

    public static class SerializedPacket {
        public byte[] data;
        public int offset;

        public SerializedPacket(@Nonnull final byte[] serialized,
                                @Nonnull final int offset) {
            this.data = serialized;
            this.offset = offset;
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
        int capacity = Math.min(buf.remaining(), command.length - commandOffset);
        byte[] cmdArr = new byte[capacity];
        System.arraycopy(command, commandOffset, cmdArr, 0, capacity);
        buf.put(cmdArr);

        byte[] bufArr = buf.array();
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
}
