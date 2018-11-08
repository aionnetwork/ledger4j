package org.aion.ledger;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

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

    private static void entry(StringBuilder builder, String entry, String value) {
        builder.append(entry);
        builder.append(" = ");
        builder.append(value);
        builder.append(", ");
    }

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

    public static String intToHex(int amount) {
        // TODO: this can be optimised
        return "0x" + bytesToHex(BigInteger.valueOf(amount).toByteArray());
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
