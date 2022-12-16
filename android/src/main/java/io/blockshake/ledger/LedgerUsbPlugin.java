package io.blockshake.ledger;

import android.content.Context;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;

import io.blockshake.ledger.operations.CloseOperation;
import io.blockshake.ledger.operations.ConnectOperation;
import io.blockshake.ledger.operations.GetDevicesOperation;
import io.blockshake.ledger.operations.MethodCallRegistry;
import io.blockshake.ledger.operations.RequestPermissionOperation;
import io.blockshake.ledger.operations.TransferInOperation;
import io.blockshake.ledger.operations.TransferOutOperation;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class LedgerUsbPlugin implements FlutterPlugin, MethodCallHandler {

    private MethodChannel channel;
    private Context context;
    private UsbManager usbManager;
    private MethodCallRegistry registry;
    private LedgerManager ledgerManager;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "ledger_usb");
        channel.setMethodCallHandler(this);
        context = binding.getApplicationContext();
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        ledgerManager = new LedgerManager(usbManager);

        // Register the method calls
        registry = new MethodCallRegistry();
        registry.registerMethodCall("getDevices", new GetDevicesOperation(ledgerManager));
        registry.registerMethodCall("requestPermission", new RequestPermissionOperation(ledgerManager));
        registry.registerMethodCall("open", new ConnectOperation(ledgerManager));
        registry.registerMethodCall("close", new CloseOperation(ledgerManager));
        registry.registerMethodCall("transferIn", new TransferInOperation(ledgerManager));
        registry.registerMethodCall("transferOut", new TransferOutOperation(ledgerManager));
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        registry.onMethodCall(context, call, result);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        registry.clear();
        registry = null;
        usbManager = null;
        ledgerManager = null;
        context = null;
    }

}
