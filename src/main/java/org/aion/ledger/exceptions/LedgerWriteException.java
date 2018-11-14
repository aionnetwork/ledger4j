package org.aion.ledger.exceptions;

public class LedgerWriteException extends Exception {
    public LedgerWriteException(String reason) {
        super(reason);
    }
}
