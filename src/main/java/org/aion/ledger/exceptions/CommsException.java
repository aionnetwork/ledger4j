package org.aion.ledger.exceptions;

import javax.annotation.Nonnull;

import static org.aion.ledger.LedgerUtilities.shortToHex;

/**
 * Indicates a communication exception
 * TODO: integration ledger exception here
 */
public class CommsException extends Exception {

    private final int respCode;

    public CommsException(@Nonnull final int code,
                          @Nonnull final String reason) {
        super("Ledger Resp Code: " + shortToHex((short) code) + ", reason: " + reason);
        this.respCode = code;
    }

    public CommsException(@Nonnull final String reason) {
        super("Ledger Resp Code: -1, reason: " + reason);
        this.respCode = -1;
    }

    public CommsException(@Nonnull final String reason,
                          @Nonnull final Throwable cause) {
        super("Ledger Resp Code: -1, reason: " + reason, cause);
        this.respCode = -1;
    }

    public int getResponseCode() {
        return this.respCode;
    }
}
