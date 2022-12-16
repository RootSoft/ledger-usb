package io.blockshake.ledger.operations;

import android.content.Context;
import android.hardware.usb.UsbManager;

import io.blockshake.ledger.LedgerManager;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class CloseOperation extends UsbMethodCallOperation {

    private final LedgerManager manager;

    public CloseOperation(LedgerManager manager) {
        super(manager.usbManager);
        this.manager = manager;
    }

    @Override
    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        try {
            manager.close();
        } catch (Exception ex) {
            manager.gracefullyReset();
        }

        result.success(true);
    }
}
