import 'dart:typed_data';

import 'package:ledger_usb/ledger_usb.dart';
import 'package:ledger_usb/src/buffer.dart';

class UsbPacker {
  Future<List<Uint8List>> pack(
    Uint8List apdu,
    int channel,
    int packetSize,
  ) async {
    final writer = ByteDataWriter();
    writer.writeUint16(apdu.length);
    writer.write(apdu);
    var data = writer.toBytes();

    final blockSize = packetSize - 5;
    final nbBlocks = (data.length / blockSize).ceil();
    data = Uint8List.fromList([
      ...data,
      ...List.filled(nbBlocks * blockSize - data.length + 1, 0),
    ]);
    final blocks = <Uint8List>[];

    for (var i = 0; i < nbBlocks; i++) {
      final head = ByteDataWriter();
      head.writeUint16(channel);
      head.writeUint8(0x05); //tag
      head.writeUint16(i);

      final chunk = data.sublist(i * blockSize, (i + 1) * blockSize);
      blocks.add(Uint8List.fromList([...head.toBytes(), ...chunk]));
    }

    return blocks;
  }

  Uint8List? getReducedResult(ResponseAcc? acc) {
    if (acc == null) {
      return null;
    }

    if (acc.length == acc.data.length) {
      return acc.data;
    }

    return null;
  }

  ResponseAcc reduceResponse(ResponseAcc? acc, int channel, Uint8List chunk) {
    final reader = ByteDataReader();
    reader.add(chunk);
    var data = acc?.data ?? Uint8List(0);
    var dataLength = acc?.length ?? 0;
    var sequence = acc?.sequence ?? 0;

    final expectedChannel = reader.readUint16();
    if (expectedChannel != channel) {
      throw ArgumentError("Invalid channel", "InvalidChannel");
    }

    final expectedTag = reader.readUint8();
    if (expectedTag != 0x05) {
      throw ArgumentError("Invalid tag", "InvalidTag");
    }

    final expectedSequence = reader.readUint16();
    if (expectedSequence != sequence) {
      throw ArgumentError("Invalid sequence", "InvalidSequence");
    }

    if (acc == null) {
      dataLength = reader.readUint16();
    }

    sequence++;
    var chunkData = chunk.sublist(acc != null ? 5 : 7);
    data = Uint8List.fromList([...data, ...chunkData]);

    if (data.length > dataLength) {
      data = data.sublist(0, dataLength);
    }

    return ResponseAcc(
      data: data,
      length: dataLength,
      sequence: sequence,
    );
  }
}
