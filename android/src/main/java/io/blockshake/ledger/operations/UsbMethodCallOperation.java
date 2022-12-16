package io.blockshake.ledger.operations;

import android.hardware.usb.UsbManager;

public abstract class UsbMethodCallOperation extends MethodCallOperation{

    protected UsbManager usbManager;

    public UsbMethodCallOperation(UsbManager usbManager) {
        this.usbManager = usbManager;
    }
}
