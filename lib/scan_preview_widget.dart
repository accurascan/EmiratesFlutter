import 'dart:async';

import 'package:accuraemirates/scan_preview_controller.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ScanPreviewWidget extends StatefulWidget {
  ScanPreviewWidget(
      {this.laserColor = 0xFF00FF00,
      this.borderColor = 0xFFFFFFFF,
      required this.onScannerCreated,
      required this.onScanResult});

  final Function(ScanPreviewController) onScannerCreated;

  final ValueChanged<Object> onScanResult;
  final int laserColor;
  final int borderColor;

  @override
  ScanPreviewWidgetState createState() => ScanPreviewWidgetState();
}

class ScanPreviewWidgetState extends State<ScanPreviewWidget> {
  final BasicMessageChannel _messageChannel =
      BasicMessageChannel("scan_preview_message", StandardMessageCodec());

  @override
  void initState() {
    super.initState();
    _messageChannel.setMessageHandler(_messageHandler);
  }

  Future<dynamic> _messageHandler(message) async {
    widget.onScanResult(message);
  }

  @override
  void dispose() {
    // controller.stopCamera();
    // WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  // @override
  // void didChangeAppLifecycleState(AppLifecycleState state) async {
  //   if (state == AppLifecycleState.resumed) {
  //     controller.startCamera();
  //   } else if (state == AppLifecycleState.paused) {
  //     controller.stopCamera();
  //   }
  // }

  @override
  Widget build(BuildContext context) {
    return _init();
  }

  Widget _init() {
    final Map<String, dynamic> creationParams = <String, dynamic>{
      // 其他参数
      'laserColor': widget.laserColor,
      'borderColor': widget.borderColor
    };
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'scan_preview',
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
        onPlatformViewCreated: onPlatformViewCreated,
      );
    } else {
      return UiKitView(
        viewType: 'scan_preview',
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
        onPlatformViewCreated: onPlatformViewCreated,
      );
    }
  }

  void onPlatformViewCreated(int id) {
    final ScanPreviewController controller =
        ScanPreviewController.init(id, this);
    widget.onScannerCreated(controller);
  }
}
