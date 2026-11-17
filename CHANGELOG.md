## Unreleased

- Toolchain: Flutter 3.50.0 (Dart 3.14.0) for development (`.tool-versions`). Dependencies were already at their latest versions; the minimum SDK constraints are unchanged, so consumers see no change
- CI: pull requests and pushes to `master` run `flutter analyze`, so lint issues fail the PR (the repo had no CI before)

## 1.1.1

- Web (WebHID): unplugging the Ledger fails the exchange with a `connectionLost` `PlatformException`
  (`ConnectionLostException` in ledger_flutter_plus) instead of a plain `Exception`, so apps can show
  "connection lost". `open()` reuses the device from the last `getDevices()` only while it is still
  connected, otherwise looks it up again: a retry after an unplug no longer waits forever on the dead
  device, and a replugged Ledger is found.
- Android: method calls run on a background task queue. The blocking USB transfers ran on the platform
  thread, which also runs the Flutter UI since Flutter 3.29, so the UI froze while waiting for the
  Ledger.

## 1.1.0

- Web: WebHID backend (`navigator.hid`), used whenever the browser supports it; WebUSB remains the
  fallback. WebUSB is unreliable for Ledger on Windows. Only the Ledger APDU HID collection (usage
  page `0xffa0`) is used; the 64-byte APDU framing is unchanged. Unplugging or closing the device
  while an exchange waits for a response fails that exchange.
- Web: `LedgerUsb.webDeviceChooserEnabled` (default `true`). When `false`, `getDevices()` never calls
  `requestDevice()` and returns an empty list if no Ledger is permitted yet. No-op on other platforms.

## 1.0.9

- Web: `getDevices()` returns already-permitted Ledger devices (`navigator.usb.getDevices()`) without
  showing the browser chooser; `requestDevice()` is only called when none is permitted yet. Lets
  extension pages that cannot show a chooser (side panel, action popup) talk to a device paired
  from a full tab.

## 1.0.8

- Updated Android gradle plugin to 9.0.0

## 1.0.7

- Fixed android 16 devices not being able to establish connection

## 1.0.6

- Updated android build tools

## 1.0.5 (RETRACTED)

Retracted due to android build bug

## 1.0.4

- PR3: Fixed Android 14 USB connection (thank you [@konstantinullrich](https://github.com/konstantinullrich))

## 1.0.3

- ~~PR3: Fixed Android 14 USB connection (thank you [@konstantinullrich](https://github.com/konstantinullrich))~~

## 1.0.2

- Updated Android gradle and compile/target sdk versions

## 1.0.1

- Added support for WEB using WebUSB API

## 1.0.0

- Initial release
