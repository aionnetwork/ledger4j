package org.aion.ledger.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO: this class must be thrown when invalid address or pk is thrown
public class InvalidKeyAddressTupleException extends Exception {

    private final byte[] address;
    private final byte[] publicKey;

    /**
     * Represents an address that is deemed invalid, this is thrown
     * in response to the ledger application giving an invalid address
     * back. Note that this is considered a syntactical difference
     *
     * @param address of the failing tuple
     * @param publicKey of the failing tuple
     * @param reason for failure
     */
    public InvalidKeyAddressTupleException(@Nullable final byte[] address,
                                           @Nullable final byte[] publicKey,
                                           @Nonnull final String reason) {
        super(reason);
        this.address = address;
        this.publicKey = publicKey;
    }

    public byte[] getAddress() {
        return this.address;
    }

    public byte[] getPublicKey() {
        return this.publicKey;
    }
}
