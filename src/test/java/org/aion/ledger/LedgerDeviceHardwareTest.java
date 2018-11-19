package org.aion.ledger;

import org.aion.ledger.application.AionApp;
import org.aion.ledger.exceptions.CommsException;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Hardware tests, requires a ledger with AION app installed to test.
 * Most of these are ported from: https://github.com/aionnetwork/aion_ledger/tree/master/test
 */
public class LedgerDeviceHardwareTest {

    // indicates we couldn't find a ledger, tests are invalid if this
    // is true
    private class LedgerNotFoundException extends RuntimeException {}

    @Test
    public void testGetAionLedgerOffsetZeroKey() throws IOException, CommsException, InterruptedException {
        LedgerDevice device = LedgerUtilities.findLedgerDevice();
        AionApp aionApp = new AionApp(device);

        if (device == null) {
            throw new LedgerNotFoundException();
        }

        KeyAddress keyAddress = aionApp.getPublicKey(0);

        assertThat(keyAddress.getPublicKey()).isNotNull();
        assertThat(keyAddress.getAddress()).isNotNull();
        System.out.println(LedgerUtilities.bytesToHex(keyAddress.getPublicKey()));
    }
}
