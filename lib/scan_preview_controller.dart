import 'package:accuraemirates/scan_preview_widget.dart';
import 'package:flutter/services.dart';

class ScanPreviewController {
  ScanPreviewWidgetState scanState;
  MethodChannel channel;

  ScanPreviewController._(this.channel, this.scanState);

  static init(int id, ScanPreviewWidgetState state) {
    assert(id != null);
    final MethodChannel channel = MethodChannel('scan_preview');
    return ScanPreviewController._(channel, state);
  }

  startCamera() async {
    String result = await channel.invokeMethod('scan#startCamera');
    print('start camera: $result');
  }

  stopCamera() async {
    String result = await channel.invokeMethod('scan#stopCamera');
    print('stop camera: $result');
  }
}
