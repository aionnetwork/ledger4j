package org.aion.ledger;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class LedgerUtilities {

    public static void printDevices() {
        List<HidDeviceInfo> devices = PureJavaHidApi.enumerateDevices();
        for (HidDeviceInfo info : devices) {
            System.out.println(deviceInfo(info));
        }
    }

    @Nonnull
    static String deviceInfo(HidDeviceInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        entry(builder, "path", info.getPath());
        entry(builder, "deviceId", info.getDeviceId());
        entry(builder, "manufacturingString", info.getManufacturerString());
        entry(builder, "productString", info.getProductString());
        entry(builder, "serialNumberString", info.getSerialNumberString());
        entry(builder, "releaseNumber", intToHex(info.getReleaseNumber()));
        entry(builder, "productId", intToHex(info.getProductId()));
        entry(builder, "usagePage", intToHex(info.getUsagePage()));
        entry(builder, "vendorId", intToHex(info.getVendorId()));
        builder.append("]");
        return builder.toString();
    }

    @Nonnull
    private static void entry(StringBuilder builder, String entry, String value) {
        builder.append(entry);
        builder.append(" = ");
        builder.append(value);
        builder.append(", ");
    }

    @Nullable
    public static LedgerDevice findLedgerDevice() throws IOException {
        List<HidDeviceInfo> infos = PureJavaHidApi.enumerateDevices();

        // implies that are there is only one ledger attached, what if multiple (?)
        for (HidDeviceInfo info : infos) {
            if (isLedger(info)) {
                HidDevice device = null;
                return new LedgerDevice(PureJavaHidApi.openDevice(info));
            }
        }
        return null;
    }

    private static boolean isLedger(HidDeviceInfo device) {
        // TODO: this area needs work, original implement not picking up Nano S
        // TODO: the second condition specified below (for fallback on Mac OS and Windows currently not included)
        // see: https://github.com/LedgerHQ/ledgerjs/blob/master/packages/hw-transport-node-hid/src/getDevices.js
        // written this way more for clarity
        if (device.getVendorId() != Constants.VENDOR_LEDGER)
            return false;

        // related discussion
        // https://github.com/signal11/hidapi/issues/385
        // TODO: this is a heuristic derived from me looking at outputs
        // might not be consistent on all platforms
        if (!device.getProductString().equals(Constants.PRODUCT_LEDGER))
            return false;
        return true;
    }

    @Nonnull
    public static String intToHex(int amount) {
        // TODO: this can be optimised
        return "0x" + bytesToHex(BigInteger.valueOf(amount).toByteArray());
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    @Nonnull
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // from: https://stackoverflow.com/questions/2648242/is-this-the-best-way-to-convert-string-hex-to-bytes
    // with some minor tweaks
    @Nonnull
    public static byte[] hexToBytes(@Nonnull String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Must pass an even number of characters.");
        }

        if (hex.substring(0, 2).equals("0x")) {
            hex = hex.substring(2);
        }

        return hexToBytes(hex.toCharArray());
    }

    @Nonnull
    private static byte[] hexToBytes(@Nonnull char[] hex) {
        if (hex.length % 2 != 0) {
            throw new IllegalArgumentException("Must pass an even number of characters.");
        }

        int length = hex.length >> 1;
        byte[] raw = new byte[length];
        for (int o = 0, i = 0; o < length; o++) {
            raw[o] = (byte) ((getHexCharValue(hex[i++]) << 4)
                    | getHexCharValue(hex[i++]));
        }
        return raw;
    }

    private static byte getHexCharValue(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        }

        if (c >= 'A' && c <= 'F') {
            return (byte) (10 + c - 'A');
        }

        if (c >= 'a' && c <= 'f') {
            return (byte) (10 + c - 'a');
        }
        throw new IllegalArgumentException("Invalid hex character");
    }

    /**
     * Returns a byte array given an int, function is guaranteed to return
     * a nonnull value that is of length 4
     *
     * @param i input integer
     * @return big-endian encoded byte array of length 4
     */
    @Nonnull
    public static byte[] toByteArray(final int i) {
        final byte[] val = new byte[4];
        val[3] = (byte) (i & 0xFF);
        val[2] = (byte) ((i >> 8) & 0xFF);
        val[1] = (byte) ((i >> 16) & 0xFF);
        val[0] = (byte) ((i >> 24) & 0xFF);
        return val;
    }

    public static byte[] toHardenedOffset(final int i) {
        byte[] offset = toByteArray(i);
        offset[0] = (byte) (offset[0] | (byte) 0x80);
        return offset;
    }
}
