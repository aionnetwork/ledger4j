package org.aion.ledger.exceptions;

import org.aion.ledger.LedgerUtilities;

import javax.annotation.Nonnull;

import static org.aion.ledger.LedgerUtilities.intToHex;

/**
 * Indicates a communication exception
 * TODO: integration ledger exception here
 */
public class CommsException extends Exception {

    private final int respCode;

    public CommsException(@Nonnull final int code,
                          @Nonnull final String reason) {
        super("Ledger Resp Code: " + intToHex(code) + ", reason: " + reason);
        this.respCode = code;
    }

    public CommsException(@Nonnull final Throwable cause) {
        super(cause);
        this.respCode = -1;
    }

    public CommsException(@Nonnull final String reason,
                          @Nonnull final Throwable cause) {
        super(reason, cause);
        this.respCode = -1;
    }

    public int getResponseCode() {
        return this.respCode;
    }
}
