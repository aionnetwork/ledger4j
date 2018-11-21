package org.aion.ledger;

import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.*;

public class LedgerUtilitiesHardwareTest {

    @Test
    public void printHardwareDevices() throws IOException {
        HidServices services = HidManager.getHidServices();
        for (org.hid4java.HidDevice device : services.getAttachedHidDevices()) {
            System.out.println(device);
        }
    }

    // this is tested against the ledger device, basically means that we need
    // a ledger device for these tests to pass
    @Test
    public void testLedgerConnected() throws IOException {
        LedgerDevice device = LedgerUtilities.findLedgerDevice();
        assertThat(device).isNotNull();
    }
}
