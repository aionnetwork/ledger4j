package org.aion.ledger;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.aion.ledger.LedgerDevice.generateBip32Path;

public class LedgerDeviceTest {
    @Test
    public void testBip32PathDerivation() {
        byte[] expectedPath = LedgerUtilities.hexToBytes("8000002C800001A9800000008000000080000000");
        assertThat(generateBip32Path(0)).isEqualTo(expectedPath);
    }
}
