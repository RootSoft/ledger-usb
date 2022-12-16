package io.blockshake.ledger.operations;

import android.content.Context;

import io.blockshake.ledger.LedgerException;
import io.blockshake.ledger.LedgerManager;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class TransferInOperation extends UsbMethodCallOperation {

    private final LedgerManager manager;

    public TransferInOperation(LedgerManager manager) {
        super(manager.usbManager);
        this.manager = manager;
    }

    @Override
    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        int length = methodCall.argument("length");
        int timeout = methodCall.argument("timeout");
        try {
            byte[] data = this.manager.transferIn(length, timeout);
            result.success(data);
        } catch (LedgerException ex) {
            result.error(ex.getErrorCode(), ex.getMessage(), null);
        }
    }
}
