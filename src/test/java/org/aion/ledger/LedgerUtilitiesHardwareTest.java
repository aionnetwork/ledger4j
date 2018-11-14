package org.aion.ledger;

import org.junit.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.*;

public class LedgerUtilitiesHardwareTest {
    // this is tested against the ledger device, basically means that we need
    // a ledger device for these tests to pass
    @Test
    public void testLedgerConnected() throws IOException {
        LedgerDevice device = LedgerUtilities.findLedgerDevice();
        assertThat(device).isNotNull();
    }
}
