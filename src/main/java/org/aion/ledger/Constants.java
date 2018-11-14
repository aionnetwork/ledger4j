package org.aion.ledger;

public class Constants {

    public static int           VENDOR_LEDGER = 0x2c97;

    // this stat is only exposed on Windows and Mac
    public static int           USAGE_PAGE_LEDGER = 0xffa0;
    public static int           CHANNEL = 0x0101;
    public static int           CLA = 0x55;

    // TODO: verify this stat is consistent on Windows and Mac
    public static String        PRODUCT_LEDGER = "Ledger Nano S";

    public static final int     PACKET_SIZE = 64;

    public static final int     LEDGER_WAIT_TIMEOUT = 500; //ms

    // this is (most likely?) specific to Ledger's AION Application
    public static final byte    INS_GET_PUBLIC_KEY = 0x02;
    public static final byte    INS_SIGN = 0x04;
    public static final byte    INS_GET_APP_CONFIGURATION = 0x06;
    public static final byte    INS_SIGN_PERSONAL_MESSAGE = 0x08;
}
