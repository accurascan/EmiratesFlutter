import 'dart:io';

import 'package:accuraemirates_example/camera_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get_version/get_version.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:toast/toast.dart';

void main() {
  runApp(MyApp());
}

MethodChannel platform = MethodChannel('buildflutter.com/platform');

class MyApp extends StatelessWidget {

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: MaterialColor(0xFFD32D39, {
          50: Color(0xFFD32D39),
          100: Color(0xFFD32D39),
          200: Color(0xFFD32D39),
          300: Color(0xFFD32D39),
          400: Color(0xFFD32D39),
          500: Color(0xFFD32D39),
          600: Color(0xFFD32D39),
          700: Color(0xFFD32D39),
          800: Color(0xFFD32D39),
          900: Color(0xFFD32D39),
        }),
        // This makes the visual density adapt to the platform that you run
        // the app on. For desktop platforms, the controls will be smaller and
        // closer together (more dense) than on mobile platforms.
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  File temostor;

  double myheight_camera_Live = 80;
  double mywidth_camera_Live = 300;
  String _projectVersion = '';
  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    getprojectVersion();
  }

  getprojectVersion() async {
    String projectVersion;
// Platform messages may fail, so we use a try/catch PlatformException.
    try {
      projectVersion = await GetVersion.projectVersion;
    } on PlatformException {
      projectVersion = 'Failed to get project version.';
    }
    setState(() {
      _projectVersion = projectVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text(widget.title),
        ),
        body: GestureDetector(
          onTap: () async {
            PermissionStatus status = await Permission.camera.request();
              if (status.isDenied) {
                Toast.show('camera permission denied', context);
              } else {
                Navigator.push(context,
                  MaterialPageRoute(builder: (context) => CameraScreen()));
              }
          },
          child: Container(
            margin: EdgeInsets.only(top: 40.0, bottom: 40.0),
            child: Column(
              children: <Widget>[
                Center(
                  child: AnimatedContainer(
                    height: myheight_camera_Live,
                    width: mywidth_camera_Live,
                    margin: EdgeInsets.all(0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      mainAxisSize: MainAxisSize.max,
                      children: <Widget>[
                        Align(
                            alignment: Alignment.center,
                            child: Text("Start Scanning",
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                    fontSize: 16.0,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.white))),
                      ],
                    ),
                    duration: Duration(milliseconds: 100),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                          colors: [const Color(0xFFD32D39), Color(0xFFff4d4d)]),
                      borderRadius: const BorderRadius.only(
                        bottomLeft: Radius.circular(10.0),
                        topRight: Radius.circular(10.0),
                        bottomRight: Radius.circular(10.0),
                        topLeft: Radius.circular(10.0),
                      ),
                      boxShadow: <BoxShadow>[
                        BoxShadow(
                            spreadRadius: 1,
                            color: Color(0xFF2c3e50).withOpacity(0.5),
                            offset: Offset(7.0, 7.0),
                            blurRadius: 10.0),
                      ],
                    ),
                  ),
                ),
                Padding(padding: EdgeInsets.only(top: 10)),
                Text(_projectVersion)
              ],
            ),
          ),
        ));
  }
}
