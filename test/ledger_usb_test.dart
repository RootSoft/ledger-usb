import 'package:flutter_test/flutter_test.dart';
import 'package:ledger_usb/ledger_usb_method_channel.dart';
import 'package:ledger_usb/ledger_usb_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockLedgerUsbPlatform with MockPlatformInterfaceMixin {}

void main() {
  final LedgerUsbPlatform initialPlatform = LedgerUsbPlatform.instance;

  test('$MethodChannelLedgerUsb is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelLedgerUsb>());
  });
}
