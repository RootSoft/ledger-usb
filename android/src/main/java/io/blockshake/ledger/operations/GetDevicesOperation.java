package io.blockshake.ledger.operations;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.blockshake.ledger.LedgerManager;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class GetDevicesOperation extends UsbMethodCallOperation {

    public GetDevicesOperation(LedgerManager manager) {
        super(manager.usbManager);
    }

    @Override
    public void onMethodCall(Context context, MethodCall methodCall, MethodChannel.Result result) {
        List<Map<String, Object>> devices = new ArrayList<>();
        for (Map.Entry<String, UsbDevice> entry : usbManager.getDeviceList().entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("identifier", entry.getKey());
            map.put("vendorId", entry.getValue().getVendorId());
            map.put("deviceId", entry.getValue().getDeviceId());
            map.put("deviceName", entry.getValue().getDeviceName());
            map.put("productId", entry.getValue().getProductId());
            map.put("productName", entry.getValue().getProductName());
            map.put("manufacturerName", entry.getValue().getManufacturerName());
            map.put("configurationCount", entry.getValue().getConfigurationCount());
            devices.add(map);
        }

        result.success(devices);
    }
}
