package io.blockshake.ledger.operations;

import android.content.Context;

import io.blockshake.ledger.LedgerException;
import io.blockshake.ledger.LedgerManager;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class TransferOutOperation extends UsbMethodCallOperation {

    private final LedgerManager manager;

    public TransferOutOperation(LedgerManager manager) {
        super(manager.usbManager);
        this.manager = manager;
    }

    @Override
    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        byte[] data = methodCall.argument("data");
        int timeout = methodCall.argument("timeout");
        try {
            int length = this.manager.transferOut(data, timeout);
            result.success(length);
        } catch (LedgerException ex) {
            result.error(ex.getErrorCode(), ex.getMessage(), null);
        }
    }
}
