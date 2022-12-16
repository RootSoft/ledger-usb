package io.blockshake.ledger.operations;

import android.content.Context;
import android.hardware.usb.UsbManager;

import io.blockshake.ledger.LedgerException;
import io.blockshake.ledger.LedgerManager;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class ConnectOperation extends UsbMethodCallOperation {

    private final LedgerManager manager;

    public ConnectOperation(LedgerManager manager) {
        super(manager.usbManager);
        this.manager = manager;
    }

    @Override
    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        String identifier = methodCall.argument("identifier");
        try {
            this.manager.open(identifier);
            result.success(true);
        } catch (LedgerException ex) {
            this.manager.gracefullyReset();
            result.error(ex.getErrorCode(), ex.getMessage(), null);
        }
    }
}
