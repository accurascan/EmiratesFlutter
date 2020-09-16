import 'package:flutter/services.dart';

class Accuraemirates {
  static const MethodChannel _channel = const MethodChannel('scan_preview');

  static startCamera() async {
    await _channel.invokeMethod('startCamera');
  }

  static stopCamera() async {
    await _channel.invokeMethod('stopCamera');
  }
}
