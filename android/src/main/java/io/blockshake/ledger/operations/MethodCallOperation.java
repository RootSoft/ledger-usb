package io.blockshake.ledger.operations;

import android.content.Context;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public abstract class MethodCallOperation {

    public abstract void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result);
}
