package org.aion.ledger.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Root class of all Ledger based exceptions, this class of exceptions
 * captures unexpected events that occur when in communication with the
 * ledger device. These can be, but are not exhausted by:
 *
 *
 */
public class LedgerException extends Exception {

    public static class LedgerInfo {
        private final byte[] apduCommand;
        private final byte[] apduResponse;

        public LedgerInfo(@Nullable final byte[] apduCommand, @Nullable final byte[] apduResponse) {
            this.apduCommand = apduCommand;
            this.apduResponse = apduResponse;
        }
    }

    private final LedgerInfo info;

    public LedgerException(@Nonnull final LedgerInfo info) {
        this.info = info;
    }

    public LedgerInfo getInfo() {
        return this.info;
    }
}
