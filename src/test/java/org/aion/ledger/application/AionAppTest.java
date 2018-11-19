package org.aion.ledger.application;

import org.aion.ledger.APDUWrapper;
import org.aion.ledger.LedgerUtilities;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.aion.ledger.LedgerUtilities.hexToBytes;
import static org.aion.ledger.application.AionApp.generateBip32Path;

public class AionAppTest {
    @Test
    public void testBip32PathDerivation() {
        byte[] expectedPath = hexToBytes("8000002C800001A9800000008000000080000000");
        assertThat(generateBip32Path(0)).isEqualTo(expectedPath);
    }

    @Test
    public void testGenerateCorrectAPDUCommand() {
        // this is address at offset 0
        byte[] expectedPublicKeyAPDU = hexToBytes("e002000015058000002c800001a9800000008000000080000000");
        byte[] genPath = AionApp.generateBip32Path(0);
        byte[] genAPDUCmd = AionApp.publicKeyAPDUCommand(genPath);
        assertThat(genAPDUCmd).isEqualTo(expectedPublicKeyAPDU);
    }

    @Test
    public void testGenerateCorrectWrappedAPDUCommand() {
        int channel = 0x0101;
        byte[] expectedWrappedPublicKeyAPDU = hexToBytes("0101050000001ae002000015058000002c800001a980000000800000008000000000000000000000000000000000000000000000000000000000000000000000");

        // generation
        byte[] expectedPublicKeyAPDU = hexToBytes("e002000015058000002c800001a9800000008000000080000000");
        byte[] genPath = AionApp.generateBip32Path(0);
        byte[] genAPDUCmd = AionApp.publicKeyAPDUCommand(genPath);
        byte[] genWrappedAPDUCmd = APDUWrapper.wrapCommandAPDU(channel, genAPDUCmd, false);
        assertThat(genWrappedAPDUCmd).isEqualTo(expectedWrappedPublicKeyAPDU);
    }


    @Test
    public void testGenerateCorrectDummyAPDUSignCommand() {
        byte[] expectedAPDUCommand = hexToBytes("e004000056058000002c800001a9800000008000000080000000f83f00a0a0185ef98ac4841900b49ad9b432af2db7235e09ec3755e5ee36e9c4947007dd89056bc75e2d6310000084aaaaaaaa8332298e8252088502540be40001");
        byte[] message = hexToBytes("f83f00a0a0185ef98ac4841900b49ad9b432af2db7235e09ec3755e5ee36e9c4947007dd89056bc75e2d6310000084aaaaaaaa8332298e8252088502540be40001");

        byte[] bip32Path = AionApp.generateBip32Path(0);
        byte[] genAPDUCmd = AionApp.signPayloadAPDUCommand(bip32Path, message);
        assertThat(genAPDUCmd).isEqualTo(expectedAPDUCommand);
    }
}
