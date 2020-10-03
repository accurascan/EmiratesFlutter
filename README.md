# Accura Flutter Emirates ID + Face Verification Android

## Flutter
1. First add Flutter plugin project in your Flutter project then add below line in pubspec.yaml.
```
dependencies:
 flutter:
      sdk: flutter
   accuraemirates:
  path: ../
```
2. Add dependencies in `pubspec.yaml`.
```
   image_picker: ^0.6.3+4       (for open system Camera)
   intl: ^0.16.1       (for date format)
   progress_dialog: ^1.2.2      (for progress dialog)
```
3. Call `camera_screen` by this code..
```
   Navigator.push(context , new MaterialPageRoute(builder: (context) => CameraScreen()));
```
4. In flutter code `camera_screen.dart` is for scanning camera screen. In this class using `ScanPreviewWidget()`.Which is used for the camera screen and returns the scan results.
```
   ScanPreviewWidget(
      onScanResult: (result) {
 },
)
```
5. For stop and restart camera add below code in `camera_screen.dart`.
```
class _CameraScreenState extends State<AppLifecycleReactor> with WidgetsBindingObserver {

 ScanPreviewController controller;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

   @override
  void dispose() {
  //for stop camera
    controller.stopCamera();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
    controller = null;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.paused) {
    //for pause camera
      controller.activitypause();
    }
    if (state == AppLifecycleState.resumed) {
    //for restart camera
      controller.activitydoOnResume();
    }
  }
}
```
6. Scanned result is shown in `result_activity.dart` file.
7. For face score using Platform Channel in `result_activity.dart`.
```
   facematch_result = await _channel.invokeMethod(
             'facematch', {
   "cameraimage": picture.path,
   "cardfaceimage": face
   });
```
`cameraimage` is camera clicked image and `cardfaceimage` is front side card face image and `facematch_result` is face match score.
# Android

#### Step 1: Add files to project assets folder
```
Create assets folder under example/android/src/main and add license file in to assets folder.
1. key.license // for Accura Emirates
2. accuraface.license // for Accura Face Match
Generate your Accura license from https://accurascan.com/developer/dashboard
```
#### Step 2: Update filters
Update filter in `setFilter()` method on `line no. 303` in `flutter_plugin_card_scan\android\src\main\java\com\docrecog\scan\RecogEngine.java` as per requirement.

   * Set Blur Percentage to allow blur on document
        ```
        //0 for clean document and 100 for Blurry document
        setBlurPercentage(40/*blurPercentage*/);
        ```
   * Set face blur Percentage to allow blur on detected Face
        ```
        // 0 for clean face and 100 for Blurry face
        setFaceBlurPercentage(40/*faceBlurPercentage*/);
        ```
   * Set Glare Percentage to detect Glare on document
        ```
        // Set min and max percentage for glare
        setGlarePercentage(5/*minPercentage*/, 90/*maxPercentage*/);
        ```
   * Set Hologram detection to verify the hologram on the face
        ```
        // true to check hologram on face
        setHologramDetection(true/*isDetectHologram*/);
        ```
   * Set light tolerance to detect light on document
        ```
        // 0 for full dark document and 100 for full bright document
        setLowLightTolerance(39/*lightTolerance*/);
        ```
   * Set motion threshold to detect motion on camera document
        ```
        // 1 - allows 1% motion on document and
        // 100 - means it can not detect motion and allow document to scan.
        setMotionThreshold(15/*motionThreshold*/);
        ```
Also update `gravThreshold` value from minimum 0.1 to maximum 1 in [SensorsActivity](https://github.com/accurascan/EmiratesFlutter/blob/master/android/src/main/java/com/docrecog/scan/SensorsActivity.java) for device motion.

## iOS

#### Step 1: Add files to project root directory
```
1. key.license // for Accura Emirates
2. accuraface.license // for Accura Face Match
Generate your Accura license from https://accurascan.com/developer/dashboard.
```
#### Step 2: Update filters
Update filter on line no.169 in VideoCameraWrapper.mm as per requirement.

 * Set Blur Percentage to allow blur on document
     ```
      //0 for clean document and 100 for Blurry document
      docrecog_scan_RecogEngine_setBlurPercentage(65/*blurPercentage*/);
    ```
 * Set face blur Percentage to allow blur on detected Face
    ```
      // 0 for clean face and 100 for Blurry face
      docrecog_scan_RecogEngine_setFaceBlurPercentage(65/*faceBlurPercentage*/);
    ```
 * Set Glare Percentage to detect Glare on document
    ```
      // Set min and max percentage for glare
      docrecog_scan_RecogEngine_setGlarePercentage(8/*minPercentage*/, 99/*maxPercentage*/);
     ```

 * Set Hologram detection to verify the hologram on the face
      ```
      // 1 to check hologram on face
      docrecog_scan_RecogEngine_setHologramDetection(1/*isDetectHologram*/);
      ```
 * Set light tolerance to detect light on document
      ```
        // 0 for full dark document and 100 for full bright document
        docrecog_scan_RecogEngine_setLowLightTolerance(39/*lightTolerance*/);
      ```
  * Set motion threshold to detect motion on camera document
      ```
        // 1 - allows 1% motion on document and
        // 100 - means it can not detect motion and allow document to scan.
        docrecog_scan_RecogEngine_setMotionThreshold(15/*motionThreshold*/);
      ```
