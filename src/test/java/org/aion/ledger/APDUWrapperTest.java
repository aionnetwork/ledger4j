package org.aion.ledger;

import org.junit.Test;
import static com.google.common.truth.Truth.*;

public class APDUWrapperTest {
    @Test
    public void testWrapCommandAPDUCheckData() {
        final int channel = 0x101;
        final int tag = 0x05;
        final int sequenceIdx = 0;
        final int commandLen = 200;

        final byte[] command = new byte[commandLen];
        for (int i = 0; i < 200; i++) {
            command[i] = (byte) (i % 256);
        }

        final byte[] out = APDUWrapper.wrapCommandAPDU(channel, command, false);
        assertThat(out.length).isEqualTo(256);

        {
            // check that payloads are correct
            final byte[] outChunk = new byte[57];
            final byte[] commandChunk = new byte[57];
            System.arraycopy(out, 7, outChunk, 0, outChunk.length);
            System.arraycopy(command, 0, commandChunk, 0, commandChunk.length);
            assertThat(outChunk).isEqualTo(commandChunk);
        }

        {
            // all other chunks gain 2 bytes, because of lack of length short
            final byte[] outChunk = new byte[59];
            final byte[] commandChunk = new byte[59];
        }
    }
}
