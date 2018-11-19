package org.aion.ledger;

import javax.annotation.Nonnull;

public class ByteUtilities {

    @Nonnull
    public static byte[] merge(@Nonnull final byte[] in1, @Nonnull final byte[] in2) {
        byte[] out = new byte[in1.length + in2.length];
        System.arraycopy(in1, 0, out, 0, in1.length);
        System.arraycopy(in2, 0, out, in1.length, in2.length);
        return out;
    }

    @Nonnull
    public static byte[] trimHead(@Nonnull final byte[] in, final int amount) {
        if (amount >= in.length) {
            return new byte[0];
        }

        byte[] out = new byte[in.length - amount];
        System.arraycopy(in, amount, out, 0, in.length - amount);
        return out;
    }

    @Nonnull
    public static byte[] trimTail(@Nonnull final byte[] in, final int amount) {
        if (amount >= in.length) {
            return new byte[0];
        }

        byte[] out = new byte[in.length - amount];
        System.arraycopy(in, 0, out, 0, out.length);
        return out;
    }
}
