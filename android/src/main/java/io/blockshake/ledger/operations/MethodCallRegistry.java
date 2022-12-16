package io.blockshake.ledger.operations;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MethodCallRegistry {
    private final Map<String, MethodCallOperation> operations;

    public MethodCallRegistry() {
        this.operations = new HashMap<>();
    }

    public void registerMethodCall(String method, MethodCallOperation operation) {
        this.operations.put(method, operation);
    }

    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        MethodCallOperation operation = this.operations.get(methodCall.method);
        if (operation == null) {
            result.notImplemented();
            return;
        }

        operation.onMethodCall(context, methodCall, result);
    }

    public void clear() {
        this.operations.clear();
    }

}
