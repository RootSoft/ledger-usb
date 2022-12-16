package io.blockshake.ledger.operations;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;

import io.blockshake.ledger.LedgerManager;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class RequestPermissionOperation extends UsbMethodCallOperation {

    public RequestPermissionOperation(LedgerManager manager) {
        super(manager.usbManager);
    }

    @Override
    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        String identifier = methodCall.argument("identifier");
        UsbDevice device = usbManager.getDeviceList().get(identifier);
        if (usbManager.hasPermission(device)) {
            result.success(true);
            return;
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                result.success(granted);
            }
        };

        context.registerReceiver(receiver, new IntentFilter(ACTION_USB_PERMISSION));

        usbManager.requestPermission(device, getPendingIntent(context));
    }

    static final String ACTION_USB_PERMISSION = "io.blockshake.ledger.USB_PERMISSION";

    PendingIntent getPendingIntent(Context context) {
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        return PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), flags);
    }
}
