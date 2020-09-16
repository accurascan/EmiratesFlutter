import UIKit
import Flutter
import AVFoundation

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    
    let status = AVCaptureDevice.authorizationStatus(for: .video)
    if status == .authorized {
    } else if status == .notDetermined  {
        AVCaptureDevice.requestAccess(for: .video) { granted in
            if granted {
 
            } else {
                print("Not granted access")
            }
        }
    }
    
    
    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
