package org.aion.ledger;

import purejavahidapi.HidDevice;

import javax.annotation.Nonnull;

public class LedgerDevice {

    private final HidDevice device;

    public LedgerDevice(@Nonnull HidDevice device) {
        this.device = device;
    }

    public void close() {
        this.device.close();
    }

    public void write(@Nonnull byte[] arg) {
        this.device.setFeatureReport(arg, arg.length);
    }

    public byte[] readChannel() {
        return null;
    }

    public void readError() {

    }

    @Override
    public String toString() {
        return LedgerUtilities.deviceInfo(this.device.getHidDeviceInfo());
    }
}
