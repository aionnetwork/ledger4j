package org.aion.ledger;

import java.io.IOException;

class App {
    public static void main(String[] args) throws IOException {
        LedgerDevice device = LedgerUtilities.findLedgerDevice();
        System.out.println(device);
    }
}