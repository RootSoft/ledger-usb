import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:ledger_usb/usb_device.dart';

import 'ledger_usb_platform_interface.dart';

/// An implementation of [LedgerUsbPlatform] that uses method channels.
class MethodChannelLedgerUsb extends LedgerUsbPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('ledger_usb');

  @override
  Future<List<UsbDevice>> getDevices() async {
    final devices =
        (await methodChannel.invokeMethod<List<dynamic>>('getDevices')) ?? [];

    return devices.map((device) => UsbDevice.fromMap(device)).toList();
  }

  @override
  Future<bool> requestPermission(UsbDevice usbDevice) async {
    final granted = await methodChannel.invokeMethod<bool>(
        'requestPermission', usbDevice.toMap());

    return granted ?? false;
  }

  @override
  Future<bool> open(UsbDevice usbDevice) async {
    final open =
        await methodChannel.invokeMethod<bool>('open', usbDevice.toMap());

    return open ?? false;
  }

  @override
  Future<bool> close() async {
    final closed = await methodChannel.invokeMethod<bool>('close');

    return closed ?? false;
  }

  @override
  Future<Uint8List?> transferIn(int packetSize, int timeout) async {
    final data = await methodChannel.invokeMethod<Uint8List?>('transferIn', {
      'length': packetSize,
      'timeout': timeout,
    });

    return data;
  }

  @override
  Future<int> transferOut(Uint8List data, int timeout) async {
    final length = await methodChannel.invokeMethod<int>(
      'transferOut',
      {
        'data': data,
        'timeout': timeout,
      },
    );

    return length ?? -1;
  }
}
