package org.aion.ledger.application;

import org.aion.ledger.KeyAddress;
import org.aion.ledger.LedgerDevice;
import org.aion.ledger.LedgerUtilities;
import org.aion.ledger.application.AionApp;
import org.aion.ledger.exceptions.CommsException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.aion.ledger.LedgerUtilities.bytesToHex;
import static org.aion.ledger.LedgerUtilities.hexToBytes;

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
        LedgerDevice device = null;

        try {
            device = LedgerUtilities.findLedgerDevice();
            AionApp aionApp = new AionApp(device);

            if (device == null) {
                throw new LedgerNotFoundException();
            }

            KeyAddress keyAddress = aionApp.getPublicKey(0);

            assertThat(keyAddress.getPublicKey()).isNotNull();
            assertThat(keyAddress.getAddress()).isNotNull();
            System.out.println(LedgerUtilities.bytesToHex(keyAddress.getPublicKey()));
        } finally {
            if (device != null) {
                device.close();
            }
        }
    }

    @Test
    public void testGetAionLedgerOffsetTenKeys() throws IOException, CommsException, InterruptedException {
        LedgerDevice device = null;

        try {
            device = LedgerUtilities.findLedgerDevice();
            AionApp aionApp = new AionApp(device);
            List<KeyAddress> keyAddressList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                keyAddressList.add(aionApp.getPublicKey(i));
                assertThat(keyAddressList.get(i).getAddress()).isNotNull();
                assertThat(keyAddressList.get(i).getPublicKey()).isNotNull();
            }
        } finally {
            if (device != null) {
                device.close();
            }
        }
    }

    @Ignore
    @Test
    public void testAionSignTransaction() throws IOException, InterruptedException, CommsException {
        // this was generated using the script in aion_ledger python tests
        // using the same inputs as the test test_verify_signature_with_data
        byte[] unsignedTransaction = hexToBytes("f83f00a0a0185ef98ac4841900b49ad9b432af2db7235e09ec3755e5ee36e9c4947007dd89056bc75e2d6310000084aaaaaaaa8332298e8252088502540be40001");
        LedgerDevice device = LedgerUtilities.findLedgerDevice();
        AionApp aionApp = new AionApp(device);

        try {
            byte[] response = aionApp.signPayload(0, unsignedTransaction);
            // TODO: verification, only done visually right now
            assertThat(response).isNotNull();
            assertThat(response.length).isEqualTo(64);
        } finally {
            device.close();
        }
        Thread.sleep(100L);
    }
}
