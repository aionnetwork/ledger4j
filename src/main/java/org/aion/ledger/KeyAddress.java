package org.aion.ledger;

import javax.annotation.Nonnull;

public class KeyAddress {
    private final byte[] publicKey;
    private final byte[] address;

    public KeyAddress(@Nonnull final byte[] publicKey, @Nonnull final byte[] address) {
        this.publicKey = publicKey;
        this.address = address;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getAddress() {
        return address;
    }
}