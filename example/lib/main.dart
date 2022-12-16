import 'package:algorand_dart/algorand_dart.dart';
import 'package:flutter/material.dart';
import 'package:ledger_algorand/ledger_algorand.dart';
import 'package:ledger_flutter/ledger.dart';
import 'package:ledger_usb/ledger_usb.dart';
import 'package:ledger_usb/usb_device.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _ledgerUsbPlugin = LedgerUsb();
  UsbDevice? device;
  final Algorand algorand = Algorand(
    algodClient: AlgodClient(
      apiUrl: AlgoExplorer.MAINNET_ALGOD_API_URL,
    ),
  );

  @override
  void initState() {
    super.initState();
  }

  Future<RawTransaction> _buildTransaction({required Address account}) async {
    final tx = await algorand.createPaymentTransaction(
      sender: account,
      receiver: account,
      amount: Algo.toMicroAlgos(1),
    );

    return tx;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextButton(
              child: const Text('List devices'),
              onPressed: () async {
                final devices = await _ledgerUsbPlugin.listDevices();
                if (devices.isEmpty) {
                  return;
                }

                setState(() {
                  device = devices.first;
                });
              },
            ),
            TextButton(
              onPressed: device != null
                  ? () async {
                      await _ledgerUsbPlugin.requestPermission(device!);
                    }
                  : null,
              child: const Text('Request permission'),
            ),
            TextButton(
              onPressed: device != null
                  ? () async {
                      await _ledgerUsbPlugin.open(device!);
                    }
                  : null,
              child: const Text('Connect'),
            ),
            TextButton(
              onPressed: device != null
                  ? () async {
                      final address = Address.fromAlgorandAddress(
                          'BYPEBY4EFT4M7GV4N34CMTFDQHHVEX27W5Y36UGCGS7HHA7VBDOEOQXCYM');

                      final tx = await _buildTransaction(account: address);
                      final operation = AlgorandSignMsgPackOperation(
                        accountIndex: 1,
                        transaction: tx.toBytes(),
                      );

                      final writer = ByteDataWriter();
                      final apdu = await operation.write(writer);

                      final response = await _ledgerUsbPlugin.exchange(
                        apdu,
                        timeout: 2000,
                      );

                      final reader = ByteDataReader();
                      reader.add(response);
                      final signature = await operation.read(reader);

                      final signedTx = SignedTransaction(
                        transaction: tx,
                        signature: signature,
                      );
                      signedTx.authAddress = address;

                      try {
                        final txId = await algorand.sendTransaction(
                          signedTx,
                          waitForConfirmation: true,
                        );
                        debugPrint(txId);
                        debugPrint(signedTx.transactionId);
                      } on AlgorandException catch (ex) {
                        debugPrint(ex.message);
                      }
                      //print(version.versionName);
                    }
                  : null,
              child: const Text('Exchange'),
            ),
            TextButton(
              onPressed: device != null
                  ? () async {
                      await _ledgerUsbPlugin.close();
                    }
                  : null,
              child: const Text('Close'),
            ),
          ],
        ),
      ),
    );
  }
}
