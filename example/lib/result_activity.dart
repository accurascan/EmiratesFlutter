import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:progress_dialog/progress_dialog.dart';
import 'package:toast/toast.dart';

import 'camera_screen.dart';

class result_activity extends StatefulWidget {
  const result_activity({Key key, this.data}) : super(key: key);

  final List<dynamic> data;

  @override
  _result_activityState createState() => _result_activityState();
}

class _result_activityState extends State<result_activity> {
  String image = "";
  ValueChanged<Object> onScanResult;

  MethodChannel _channel = const MethodChannel('scan_preview');

  static const BasicMessageChannel<String> platform =
      BasicMessageChannel<String>('scan_preview', StringCodec());
  String MRZ = "";

  String Document1 = "";

  String givenname = "";
  String surname = "";

  String docnumber = "";

  String docchecksum = "";

  String country = "";

  String nationality = "";

  String sex = "";

  String birthDate = "";

  String birthchecksum = "";

  String expirationchecksum = "";

  String expiryDate = "";

  var otherid = "";

  var otheridchecksum = "";

  var secondrowchecksum = "";

  var Result = "";

  String face = "";

  String frontBitmap = "";

  String BackImage = "";

  File picture = null;

  String facematch_result = "";

  String facecheckimage = "";

  String match_score = "";

  bool match_score_visiblity = false;
  bool macth_image = false;

  ProgressDialog pr;

  bool scroll_to = false;

  var scroll_controller = new ScrollController();
  final BasicMessageChannel _messageChannel =
      BasicMessageChannel("scan_preview_message", StringCodec());

  double fontsize = 16;

  File frontImageFile;
  File backImageFile;
  File faceImageFile;

  String mrzdata = "";

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    pr = ProgressDialog(context, isDismissible: false);
    pr.style(
//      message: 'Downloading file...',
      message: 'Please wait....',
    );
    mrzdata = "";
    widget.data.asMap().forEach((inxex, element) {
      element.forEach((key, value) {
        if (key == 'frontBitmap') {
          setState(() {
            frontBitmap = value.toString();
          });
        }
        if (key == 'BackImage') {
          setState(() {
            BackImage = value.toString();
          });
        }
        if (key == 'face') {
          setState(() {
            face = value.toString();
          });
        }
        if (key == 'MRZ') {
          setState(() {
            MRZ = value.toString();
          });
        }
        if (key == 'docType') {
          setState(() {
            Document1 = value.toString();
          });
        }
        if (key == 'givenname') {
          setState(() {
            givenname = value.toString();
          });
        }
        if (key == 'surname') {
          setState(() {
            surname = value.toString();
          });
        }
        if (key == 'docnumber') {
          setState(() {
            docnumber = value.toString();
          });
        }
        if (key == 'docchecksum') {
          setState(() {
            docchecksum = value.toString();
          });
        }
        if (key == 'country') {
          setState(() {
            country = value.toString();
          });
        }
        if (key == 'nationality') {
          setState(() {
            nationality = value.toString();
          });
        }
        if (key == 'sex') {
          setState(() {
            sex = value.toString();
          });
        }
        if (key == 'birthDate') {
          setState(() {
            birthDate = value;
          });
        }
        if (key == 'birthchecksum') {
          setState(() {
            birthchecksum = value;
          });
        }
        if (key == 'expirationchecksum') {
          setState(() {
            expirationchecksum = value;
          });
        }
        if (key == 'expiryDate') {
          setState(() {
            expiryDate = value;
          });
        }
        if (key == 'otherid') {
          setState(() {
            otherid = value;
          });
        }
        if (key == 'otheridchecksum') {
          setState(() {
            otheridchecksum = value;
          });
        }
        if (key == 'secondrowchecksum') {
          setState(() {
            secondrowchecksum = value;
          });
        }
        if (key == 'Result') {
          setState(() {
            Result = value;
          });
        }
      });
    });

    return Scaffold(
      appBar: AppBar(
        title: Text("Result"),
      ),
      body: WillPopScope(
          child: Container(
            child: Column(
              children: [
                Expanded(
                  flex: 12,
                  child: SingleChildScrollView(
                    controller: scroll_controller,
                    scrollDirection: Axis.vertical,
                    child: Container(
                      child: Column(
                        children: [
                          Container(
                            alignment: Alignment.center,
                            margin: EdgeInsets.only(top: 10),
                            height: 100,
                            child: Row(
                              children: [
                                Expanded(
                                  flex: 1,
                                  child: FittedBox(
                                    child: Container(
                                        width: 100,
                                        height: 100,
                                        child: Image.memory(
                                          base64Decode(face),
                                          fit: BoxFit.fill,
                                        )),
                                  ),
                                ),
                                Container(
                                  child: Visibility(
                                    visible: macth_image,
                                    child: Expanded(
                                        flex: 1,
                                        child: FittedBox(
                                            child: Container(
                                                width: 100,
                                                height: 100,
                                                child: Image.memory(
                                                  facecheckimage != '0.0'
                                                      ? base64Decode(
                                                          facecheckimage)
                                                      : base64Decode(""),
                                                  fit: BoxFit.fill,
                                                )))),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Container(
                            margin: EdgeInsets.all(10),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                Container(
                                  margin: EdgeInsets.only(bottom: 10),
                                  child: Visibility(
                                    visible: match_score_visiblity,
                                    child: Container(
                                      padding: EdgeInsets.all(7),
                                      decoration: new BoxDecoration(
                                          color: Color(0xFFD32D39),
                                          //new Color.fromRGBO(255, 0, 0, 0.0),
                                          borderRadius: new BorderRadius.all(
                                              const Radius.circular(10.0))),
                                      child: Row(
                                        // mainAxisAlignment:
                                        //     MainAxisAlignment.,
                                        mainAxisSize: MainAxisSize.max,
                                        children: [
                                          Container(
                                            padding: EdgeInsets.all(5),
                                            child: Text(
                                              "FACEMATCH SCORE : " +
                                                  match_score,
                                              style: TextStyle(
                                                  color: Colors.white,
                                                  fontWeight: FontWeight.bold,
                                                  fontSize: fontsize),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  ),
                                ),
                                Column(
                                  children: [
                                    Container(
                                      color: Color(0xFFD32D39),
                                      child: Row(
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        mainAxisSize: MainAxisSize.max,
                                        children: [
                                          Container(
                                            padding: EdgeInsets.all(5),
                                            child: Text(
                                              "MRZ",
                                              style: TextStyle(
                                                  color: Colors.white,
                                                  fontWeight: FontWeight.bold,
                                                  fontSize: fontsize),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    Table(
                                      border: TableBorder.all(
                                          color: Color(0xFFD32D39)),
                                      children: [
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('MRZ',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(MRZ,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('DOCUMENT',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(Document1,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('LAST NAME',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(surname,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('FIRST NAME',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(givenname,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('DOCUMENT NO',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(docnumber,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(
                                                    'DOCUMENT CHECK NUMBER',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(docchecksum,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('COUNTRY',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(country,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('NATIONALITY',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(nationality,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('SEX',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(sex,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('DATE OF BIRTH',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(birthDate,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(
                                                    'BIRTH CHECK NUMBER',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(birthchecksum,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('DATE OF EXPIRY ',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(expiryDate,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Container(
                                              alignment: Alignment.center,
                                              padding: EdgeInsets.all(10),
                                              child: Text(
                                                  'EXPIRATON CHECK NUMBER',
                                                  textAlign: TextAlign.center,
                                                  style: TextStyle(
                                                      fontWeight:
                                                          FontWeight.bold,
                                                      fontSize: fontsize)),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(expirationchecksum,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('OTHER ID',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(otherid,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text('OTHER ID CHECK',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(otheridchecksum,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                        TableRow(children: [
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(
                                                    'SECOND ROW CHECKSUM NO',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                          TableCell(
                                            verticalAlignment:
                                                TableCellVerticalAlignment
                                                    .middle,
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Container(
                                                padding: EdgeInsets.all(10),
                                                child: Text(secondrowchecksum,
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: fontsize)),
                                              ),
                                            ),
                                          ),
                                        ]),
                                      ],
                                    )
                                  ],
                                )
                              ],
                            ),
                          ),
                          Card(
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10.0),
                            ),
                            margin: EdgeInsets.all(10),
                            child: Column(
                              children: [
                                Container(
                                  child: Column(
                                    children: [
                                      Row(
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        mainAxisSize: MainAxisSize.max,
                                        children: [
                                          Container(
                                            padding: EdgeInsets.all(10),
                                            child: Text(
                                              "FRONT SIDE",
                                              style: TextStyle(
                                                  color: Color(0xFFD32D39),
                                                  fontWeight: FontWeight.bold,
                                                  fontSize: fontsize),
                                            ),
                                          ),
                                        ],
                                      ),
                                      Container(
                                        margin: EdgeInsets.only(
                                            left: 10, right: 10, bottom: 10),
                                        child: Image.memory(
                                            base64Decode(frontBitmap)),
                                      )
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Card(
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10.0),
                            ),
                            margin: EdgeInsets.all(10),
                            child: Column(
                              children: [
                                Container(
                                  child: Column(
                                    children: [
                                      Row(
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        mainAxisSize: MainAxisSize.max,
                                        children: [
                                          Container(
                                            padding: EdgeInsets.all(10),
                                            child: Text(
                                              "BACK SIDE",
                                              style: TextStyle(
                                                  color: Color(0xFFD32D39),
                                                  fontWeight: FontWeight.bold,
                                                  fontSize: fontsize),
                                            ),
                                          ),
                                        ],
                                      ),
                                      Container(
                                        margin: EdgeInsets.only(
                                            left: 10, right: 10, bottom: 10),
                                        child: Image.memory(
                                            base64Decode(BackImage)),
                                      )
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                Expanded(
                  flex: 1,
                  child: GestureDetector(
                    onTap: () async {
                      pr.show();
                      picture = await ImagePicker.pickImage(
                        source: ImageSource.camera,
                      );
                      if (picture != null) {
                        facecheckimage = await _channel.invokeMethod(
                            'facecrop', {
                          "cameraimage": picture.path,
                          "cardfaceimage": face
                        });

                        if (facecheckimage != "") {
                          facematch_result =
                              await _channel.invokeMethod('facematch');

                          if (facematch_result != null) {
                            setState(await () {
                              pr.hide();
                              scroll_to = false;
                              match_score = facematch_result;
                              match_score_visiblity = true;
                              if (match_score != "0.0" &&
                                  match_score != "0.00") {
                                macth_image = true;
                              } else {
                                macth_image = false;
                              }

                              scroll_controller.jumpTo(0);
                            });
                          } else {
                            setState(() {
                              pr.hide();
                              Toast.show("Please, Try again", context);
                            });
                          }
                        } else {
                          pr.hide();
                          Toast.show("Please, Try again", context);
                        }
                      } else {
                        pr.hide();
                      }
                    },
                    child: Container(
                      margin: EdgeInsets.only(
                          top: 5, bottom: 5, left: 20, right: 20),
                      padding: EdgeInsets.all(5),
                      decoration: new BoxDecoration(
                        shape: BoxShape.rectangle,
                        borderRadius: new BorderRadius.only(
                          topLeft: new Radius.circular(20.0),
                          topRight: new Radius.circular(20.0),
                          bottomRight: new Radius.circular(20.0),
                          bottomLeft: new Radius.circular(20.0),
                        ),
                        gradient: new LinearGradient(
                          colors: [const Color(0xFFD32D39), Color(0xFFff4d4d)],
                        ),
                      ),
                      child: Align(
                        alignment: Alignment.center,
                        child: Text(
                          "Face Match",
                          style: TextStyle(color: Colors.white),
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          onWillPop: () {
            Navigator.pop(context);
            Navigator.push(context,
                new MaterialPageRoute(builder: (context) => CameraScreen()));
          }),

//      Image.memory(base64Decode(image)),
    );
  }
}
