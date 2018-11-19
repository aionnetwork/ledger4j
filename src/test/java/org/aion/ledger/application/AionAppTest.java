package org.aion.ledger.application;

import org.aion.ledger.APDUWrapper;
import org.aion.ledger.LedgerDevice;
import org.aion.ledger.LedgerUtilities;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.aion.ledger.application.AionApp.generateBip32Path;

public class AionAppTest {
    @Test
    public void testBip32PathDerivation() {
        byte[] expectedPath = LedgerUtilities.hexToBytes("8000002C800001A9800000008000000080000000");
        assertThat(generateBip32Path(0)).isEqualTo(expectedPath);
    }

    @Test
    public void testGenerateCorrectAPDUCommand() {
        // this is address at offset 0
        byte[] expectedPublicKeyAPDU = LedgerUtilities.hexToBytes("e002000015058000002c800001a9800000008000000080000000");
        byte[] genPath = AionApp.generateBip32Path(0);
        byte[] genAPDUCmd = AionApp.publicKeyAPDUCommand(genPath);

        assertThat(genAPDUCmd).isEqualTo(expectedPublicKeyAPDU);
    }

    @Test
    public void testGenerateCorrectWrappedAPDUCommand() {
        int channel = 0x0101;
        byte[] expectedWrappedPublicKeyAPDU = LedgerUtilities.hexToBytes("0101050000001ae002000015058000002c800001a980000000800000008000000000000000000000000000000000000000000000000000000000000000000000");

        // generation
        byte[] expectedPublicKeyAPDU = LedgerUtilities.hexToBytes("e002000015058000002c800001a9800000008000000080000000");
        byte[] genPath = AionApp.generateBip32Path(0);
        byte[] genAPDUCmd = AionApp.publicKeyAPDUCommand(genPath);
        byte[] genWrappedAPDUCmd = APDUWrapper.wrapCommandAPDU(channel, genAPDUCmd, false);
        assertThat(genWrappedAPDUCmd).isEqualTo(expectedWrappedPublicKeyAPDU);
    }
}
