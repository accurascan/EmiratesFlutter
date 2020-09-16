import 'dart:async';
import 'dart:math';
import 'dart:ui';

import 'package:accuraemirates/scan_preview_controller.dart';
import 'package:accuraemirates/scan_preview_widget.dart';
import 'package:accuraemirates_example/result_activity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

MethodChannel platform = MethodChannel('scan_preview');

class CameraScreen extends StatefulWidget {
  @override
  _CameraScreenState createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen>
    with SingleTickerProviderStateMixin {
  List cameras;
  List cameras1;
  String imagePath;
  bool is_capture = true;
  ScanPreviewController controller;

  var count = 0;

  List<dynamic> Frontdata;

  var Backdata;

  int back = 1;

  bool _isDetecting = false;

  bool done = false;

  AnimationController controller_anim;

  bool isFrontVisible = true;

  Animation<double> _frontRotation;

  bool animimage_visible = false;

  int License_state = -1;
  String message_toast = " ";
  String message_card_side = " ";

  var overlay_size;

  var toast = "";
  var side_message = "Scan Front side of Emirates National ID";

  List<dynamic> result_data;

  AppBar appBar;

  @override
  void initState() {
    super.initState();

    controller_anim = AnimationController(
        duration: Duration(milliseconds: 2000), vsync: this);

    _updateRotations(true);
  }

  Future<Null> _getLicense() async {
    overlay_size = await platform.invokeMethod('overlay');
    if (overlay_size != null) {
      int License = -1;
      try {
        final int result = await platform.invokeMethod('getResult');
        License = result;
      } on PlatformException catch (e) {
        License = -1;
      }

      setState(() {
        License_state = License;
        if (License_state != 0) {
          _showDialog(License_state);
        } else {}
        print("result     :" + License_state.toString());
      });
    }
  }

  void _showDialog(int license) {
    // flutter defined function
    showDialog(
      context: context,
      builder: (BuildContext context) {
        // return object of type Dialog
        return AlertDialog(
          content: message(license),
          actions: <Widget>[
            // usually buttons at the bottom of the dialog
            new FlatButton(
              child: new Text("Ok"),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Widget message(int ret) {
    if (ret == -1) {
      return new Text("No Key Found");
    } else if (ret == -2) {
      return new Text("Invalid Key");
    } else if (ret == -3) {
      return new Text("Invalid Platform");
    } else if (ret == -4) {
      return new Text("Invalid License");
    }
  }

  @override
  void dispose() {
    controller.stopCamera();
    controller = null;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    setState(() {
      message_toast = toast;
      message_card_side = side_message;
    });

    appBar = AppBar(
      title: Text("Camera"),
    );

    return SafeArea(
      child: Scaffold(
        appBar: appBar,
        body: _cameraPreviewWidget(),
      ),
    );
  }

  Widget _cameraPreviewWidget() {
    double appBar_height = appBar.preferredSize.height;
    var padding = MediaQuery.of(context).padding;
    double height = MediaQuery.of(context).size.height;
    double height_camera = MediaQuery.of(context).size.height -
        appBar_height -
        padding.top -
        padding.bottom;
    final width = MediaQuery.of(context).size.width;
//    final height = MediaQuery.of(context).size.height;

    final heightw = 964 / 570;
    final width_padding = (width / 20);

    final new_width = width - width_padding;
    final new_height = new_width / heightw;

    controller_anim.addStatusListener((status) {
      if (status == AnimationStatus.completed) {
        animimage_visible = false;
      }
    });

    return Stack(
      children: [
        Container(
          width: width,
          height: height,
          child: ClipRect(
            child: OverflowBox(
              alignment: Alignment.center,
              child: FittedBox(
                fit: BoxFit.fitWidth,
                child: Container(
                  width: width,
                  height: height_camera,
                  child: ScanPreviewWidget(
                    onScannerCreated: (ScanPreviewController controller) {
                      this.controller = controller;
                      controller.startCamera();
                    },
                    onScanResult: (result) {
                      result_data = result as List<dynamic>;

                      result_data.forEach((element) {
                        element.forEach((key, value) {
                          if (key == 'message') {
                            setState(() {
                              _isDetecting = false;

                              switch (value) {
                                case "0":
                                  toast = "Keep Document Steady";
                                  break; // if device in motion
                                case "1":
                                  toast = "Keep document in frame";
                                  break;
                                case "2":
                                  toast = "Bring card near to frame.";
                                  break;
                                case "3":
                                  toast = "Processing...";
                                  break;
                                case "4":
                                  toast = "Blur detect in document";
                                  break;
                                case "5":
                                  toast = "Blur detected over face";
                                  break;
                                case "6":
                                  toast = "Glare detect in document";
                                  break;
                                case "7":
                                  toast = "Hologram Detected";
                                  break;
                                case "8":
                                  toast = "Low lighting detected";
                                  break;
                                default:
                                  toast = value; // some filter message
                              }
                            });
                          }
                          if (key == 'front' && value == "done") {
                            setState(() async {
                              animimage_visible = true;
                              side_message =
                                  "Scan Back side of Emirates National ID";
                              _leftRotation();
                            });
                          }
                          print("length   : " + result_data.length.toString());
                          if ((result_data.length >= 19 ||
                                  result_data[0].length >= 19) &&
                              key == 'BackImage' &&
                              value != "") {
                            Navigator.pop(context);
                            Navigator.push(
                              context,
                              new MaterialPageRoute(
                                  builder: (context) =>
                                      result_activity(data: result_data)),
                            );
                          }
                        });
                      });
                    },
                  ),
                ),
              ),
            ),
          ),
        ),
        Container(
          decoration: ShapeDecoration(
            shape: _ScannerOverlayShape(
              width: 964,
              height: 570,
              full_width: width,
              full_height: height,
              borderColor: Theme.of(context).primaryColor,
              borderWidth: 3.0,
            ),
          ),
        ),
        AnimatedBuilder(
          animation: _frontRotation,
          builder: (BuildContext context, Widget child) {
            var transform = Matrix4.identity();
            transform.setEntry(3, 2, 0.001);
            transform.rotateY(_frontRotation.value);
            return Transform(
              transform: transform,
              alignment: Alignment.center,
              child: Visibility(
                visible: animimage_visible,
                child: Center(
                  child: Container(
                    width: 80,
                    height: 80,
                    child: Image.asset("assets/flip.png"),
                  ),
                ),
              ),
            );
          },
        ),
        Align(
          alignment: Alignment.topCenter,
          child: Container(
            margin: EdgeInsets.only(top: ((height + 80) / 2) - (new_height)),
            child: Text(
              message_card_side.isEmpty ? "" : message_card_side,
              style: TextStyle(color: Colors.white, fontSize: 16),
            ),
          ),
        ),
        Align(
          alignment: Alignment.topCenter,
          child: Container(
            margin: EdgeInsets.only(top: height - (height / 2.6)),
            child: Text(
              message_toast.isEmpty ? "" : message_toast,
              style: TextStyle(color: Colors.white),
            ),
          ),
        )
      ],
    );
  }

  void _leftRotation() {
    _toggleSide(false);
  }

  void _rightRotation() {
    _toggleSide(true);
  }

  void _toggleSide(bool isRightTap) {
    _updateRotations(isRightTap);
    if (isFrontVisible) {
      controller_anim.forward();
      isFrontVisible = false;
    } else {
      controller_anim.reverse();
      isFrontVisible = true;
    }
  }

  _updateRotations(bool isRightTap) {
    setState(() {
      bool rotateToLeft =
          (isFrontVisible && !isRightTap) || !isFrontVisible && isRightTap;
      _frontRotation = TweenSequence(
        <TweenSequenceItem<double>>[
          TweenSequenceItem<double>(
            tween: Tween(begin: 0.0, end: rotateToLeft ? (pi) : (-pi))
                .chain(CurveTween(curve: Curves.linear)),
            weight: 50.0,
          ),
          TweenSequenceItem<double>(
            tween: ConstantTween<double>(rotateToLeft ? (-pi) : (pi)),
            weight: 50.0,
          ),
//          TweenSequenceItem<double>(
//            tween: ConstantTween<double>(rotateToLeft ? (-pi / 2) : (pi / 2)),
//            weight: 50.0,
//          ),
//          TweenSequenceItem<double>(
//            tween: Tween(begin: 0.0, end: rotateToLeft ? (pi / 2) : (-pi / 2))
//                .chain(CurveTween(curve: Curves.linear)),
//            weight: 50.0,
//          ),
        ],
      ).animate(controller_anim);
//      _backRotation = TweenSequence(
//        <TweenSequenceItem<double>>[
//          TweenSequenceItem<double>(
//            tween: ConstantTween<double>(rotateToLeft ? (pi / 2) : (-pi / 2)),
//            weight: 50.0,
//          ),
//          TweenSequenceItem<double>(
//            tween: Tween(begin: rotateToLeft ? (-pi / 2) : (pi / 2), end: 0.0)
//                .chain(CurveTween(curve: Curves.linear)),
//            weight: 50.0,
//          ),
//        ],
//      ).animate(controller_anim);
    });
  }
}

class _ScannerOverlayShape extends ShapeBorder {
  final Color borderColor;
  final double width;
  final double height;
  final double full_width;
  final double full_height;
  final double borderWidth;
  final Color overlayColor;

  _ScannerOverlayShape({
    this.width,
    this.height,
    this.full_width,
    this.full_height,
    this.borderColor = Colors.white,
    this.borderWidth = 1.0,
    this.overlayColor = const Color(0x88000000),
  });

  @override
  EdgeInsetsGeometry get dimensions => EdgeInsets.all(10.0);

  @override
  Path getInnerPath(Rect rect, {TextDirection textDirection}) {
    return Path()
      ..fillType = PathFillType.evenOdd
      ..addPath(getOuterPath(rect), Offset.zero);
  }

  @override
  Path getOuterPath(Rect rect, {TextDirection textDirection}) {
    Path _getLeftTopPath(Rect rect) {
      return Path()
        ..moveTo(rect.left, rect.bottom)
        ..lineTo(rect.left, rect.top)
        ..lineTo(rect.right, rect.top);
    }

    return _getLeftTopPath(rect)
      ..lineTo(
        rect.right,
        rect.bottom,
      )
      ..lineTo(
        rect.left,
        rect.bottom,
      )
      ..lineTo(
        rect.left,
        rect.top,
      );
  }

  @override
  void paint(Canvas canvas, Rect rect, {TextDirection textDirection}) {
    const lineSize = 10;

//    final width = rect.width;
    final width = this.width;
    final borderWidthSize = width * 10 / 100;
//    final height = rect.height;
    final height = this.height;
    final heightw = this.width / this.height;
    final width_padding = (full_width / 20);

    final new_width = full_width - width_padding;
    final new_height = new_width / heightw;

    final borderHeightSize = height - (width - borderWidthSize);
    final borderSize = Size(borderWidthSize / 2, borderHeightSize / 2);

    var paint = Paint()
      ..color = overlayColor
      ..style = PaintingStyle.fill;

    canvas
      ..drawRect(
        Rect.fromLTRB(rect.left, rect.top, rect.width,
            ((this.full_height + rect.top) / 2) - (new_height / 2)),
        paint,
      )
      ..drawRect(
        Rect.fromLTRB(
            rect.left,
            ((this.full_height + rect.top) / 2) + (new_height / 2),
            rect.right,
            rect.bottom),
        paint,
      )
      ..drawRect(
        Rect.fromLTRB(
            rect.left,
            ((this.full_height + rect.top) / 2) - (new_height / 2),
            this.full_width - new_width,
            ((this.full_height + rect.top) / 2) + (new_height / 2)),
        paint,
      )
      ..drawRect(
        Rect.fromLTRB(
            this.full_width - width_padding,
            ((this.full_height + rect.top) / 2) - (new_height / 2),
            rect.right,
            ((this.full_height + rect.top) / 2) + (new_height / 2)),
        paint,
      );

    paint = Paint()
      ..color = borderColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = borderWidth;

    final borderOffset = borderWidth / 2;
    final realReact = Rect.fromLTRB(
        this.full_width - new_width,
        ((this.full_height + rect.top) / 2) - (new_height / 2),
        this.full_width - width_padding,
        ((this.full_height + rect.top) / 2) + (new_height / 2));

    //Draw top right corner
    canvas
      ..drawPath(
          Path()
            ..moveTo(realReact.right, realReact.top)
            ..lineTo(realReact.right, realReact.bottom),
          paint)
//      ..drawPath(
//          Path()
//            ..moveTo(realReact.right, realReact.top)
//            ..lineTo(realReact.right - lineSize, realReact.top),
//          paint)
      ..drawPoints(
        PointMode.points,
        [Offset(realReact.right, realReact.top)],
        paint,
      )

      //Draw top left corner
      ..drawPath(
          Path()
            ..moveTo(realReact.left, realReact.top)
            ..lineTo(realReact.left, realReact.top + lineSize),
          paint)
      ..drawPath(
          Path()
            ..moveTo(realReact.left, realReact.top)
            ..lineTo(realReact.right, realReact.top),
          paint)
      ..drawPoints(
        PointMode.points,
        [Offset(realReact.left, realReact.top)],
        paint,
      )

      //Draw bottom right corner
//      ..drawPath(
//          Path()
//            ..moveTo(realReact.right, realReact.bottom)
//            ..lineTo(realReact.right, realReact.bottom - lineSize),
//          paint)
      ..drawPath(
          Path()
            ..moveTo(realReact.right, realReact.bottom)
            ..lineTo(realReact.left, realReact.bottom),
          paint)
      ..drawPoints(
        PointMode.points,
        [Offset(realReact.right, realReact.bottom)],
        paint,
      )

      //Draw bottom left corner
      ..drawPath(
          Path()
            ..moveTo(realReact.left, realReact.bottom)
            ..lineTo(realReact.left, realReact.top),
          paint)
//      ..drawPath(
//          Path()
//            ..moveTo(realReact.left, realReact.bottom)
//            ..lineTo(realReact.left + lineSize, realReact.bottom),
//          paint)
      ..drawPoints(
        PointMode.points,
        [Offset(realReact.left, realReact.bottom)],
        paint,
      );
  }

  @override
  ShapeBorder scale(double t) {
    return _ScannerOverlayShape(
      borderColor: borderColor,
      borderWidth: borderWidth,
      overlayColor: overlayColor,
    );
  }
}
