import 'dart:math';
import 'dart:typed_data';

import 'package:ledger_usb/src/usb_packer.dart';
import 'package:ledger_usb/usb_device.dart';

import 'ledger_usb_platform_interface.dart';

///
/// DOCS: https://developers.ledger.com/docs/transport/web-hid-usb/
/// WEB USB: https://github.com/LedgerHQ/ledger-live/blob/develop/libs/ledgerjs/packages/hw-transport-webusb/src/TransportWebUSB.ts
/// HID Framing: https://github.com/LedgerHQ/ledger-live/blob/develop/libs/ledgerjs/packages/devices/src/hid-framing.ts
class LedgerUsb {
  Future<List<UsbDevice>> listDevices() {
    return LedgerUsbPlatform.instance.getDevices();
  }

  Future<bool> requestPermission(UsbDevice usbDevice) {
    return LedgerUsbPlatform.instance.requestPermission(usbDevice);
  }

  Future<bool> open(UsbDevice usbDevice) {
    return LedgerUsbPlatform.instance.open(usbDevice);
  }

  Future<Uint8List> exchange(
    List<Uint8List> apdus, {
    int timeout = 2000,
  }) async {
    final packer = UsbPacker();
    final output = <Uint8List>[];

    // Frame the packets
    final channel = (Random().nextDouble() * 0xffff).floor();
    const packetSize = 64;

    for (var apdu in apdus) {
      final blocks = await packer.pack(apdu, channel, packetSize);

      for (var block in blocks) {
        await LedgerUsbPlatform.instance.transferOut(block, timeout);
      }

      ResponseAcc? acc;
      while (packer.getReducedResult(acc) == null) {
        final response =
            await LedgerUsbPlatform.instance.transferIn(packetSize, timeout);
        if (response != null && response.isNotEmpty) {
          acc = packer.reduceResponse(acc, channel, response);
        }
      }

      final result = packer.getReducedResult(acc) ?? Uint8List.fromList([]);
      // TODO only algorand? - 2 is uint16 for response code
      int offset = (result.length >= 2) ? 2 : 0;

      output.add(result.sublist(0, result.length - offset));
    }

    return Uint8List.fromList(output.expand((e) => e).toList());
  }

  Future<bool> close() {
    return LedgerUsbPlatform.instance.close();
  }
}

class ResponseAcc {
  final Uint8List data;
  final int length;
  final int sequence;

  ResponseAcc({
    required this.data,
    required this.length,
    required this.sequence,
  });
}
