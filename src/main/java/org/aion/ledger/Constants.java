package org.aion.ledger;

public class Constants {
    public static boolean LIB_NATIVE = true;

    public static int           VENDOR_LEDGER = 0x2c97;

    // this stat is only exposed on Windows and Mac
    public static int           USAGE_PAGE_LEDGER = 0xffffffa0;
    public static int           CHANNEL = 0x0101;

    public static int           INTERFACE_NUMBER = 0;

    // TODO: verify this stat is consistent on Windows and Mac
    public static String        PRODUCT_LEDGER = "Nano S";

    public static final int     PACKET_SIZE = 64;

    public static final int     LEDGER_WAIT_TIMEOUT = 500; //ms
}
