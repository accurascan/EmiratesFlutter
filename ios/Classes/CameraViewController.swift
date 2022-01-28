//
//  ViewController.swift
//  AccuraSDK
//
//  Created by Chang Alex on 1/24/20.
//  Copyright © 2020 Elite Development LLC. All rights reserved.
//

import UIKit
import Flutter
import AVFoundation

public class SwiftScanPreviewViewFactory: NSObject, FlutterPlatformViewFactory{
    var binaryMessenger: FlutterBinaryMessenger!
    
    @objc public init(binaryMessenger: FlutterBinaryMessenger) {
        self.binaryMessenger = binaryMessenger
        super.init()
    }
    
    public func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
        return SwiftScanPreivew(frame:frame, binaryMessenger: binaryMessenger);
    }
    
}

public class SwiftScanPreivew:NSObject, FlutterPlatformView {
    var cameraViewController: CameraViewController;
    public init(frame: CGRect,binaryMessenger: FlutterBinaryMessenger) {
        cameraViewController = CameraViewController(frame: frame, binaryMessenger:binaryMessenger)
    }
    
    public func view() -> UIView {
        return cameraViewController.imageView;
    }
}

public class CameraViewController: UIViewController{
    
    
    //MARK:- Variable
    var _channel: FlutterMethodChannel!;
    var _messagingChannel: FlutterBasicMessageChannel!;
    var faceImage: UIImage?
    var camaraImage: UIImage?
    var faceRegion: NSFaceRegion?
    var imageView: UIImageView!
    
    var videoCameraWrapper: VideoCameraWrapper? = nil
    
    var shareScanningListing: NSMutableDictionary = [:]
    
    var dictResult : [String : String] = [String : String]()
    var imgViewCard : UIImage?
    var isCheckCard : Bool = false
    
    var isCheckcardBack : Bool = false
    var isCheckCardBackFrint : Bool = false
    
    var isBack : Bool?
    var isFront : Bool?
    
    var imgViewCardFront : UIImage?
    
    var dictFrontResult : [String : String] = [String : String]()
    var dictBackResult : [String : String] = [String : String]()
    
    var dictBackResult11 : [[String : String]] = [[String : String]]()
    
    var isflipanimation : Bool?
    
    var imgPhoto : UIImage?
    var isCheckFirstTime : Bool = false
    var setImage : Bool?
    var _imageView1: UIImageView?
    var dictScanningData: [String : String] = [String : String]()
    
    var face2 : NSFaceRegion?
    var imageFace: UIImage?
    
    init(frame: CGRect, binaryMessenger: FlutterBinaryMessenger) {
        _channel = FlutterMethodChannel(name: "scan_preview", binaryMessenger: binaryMessenger);
        _messagingChannel = FlutterBasicMessageChannel(name: "scan_preview_message", binaryMessenger: binaryMessenger);
        super.init(nibName: nil, bundle: nil);
        _channel.setMethodCallHandler { (call: FlutterMethodCall, result: @escaping FlutterResult) in
            switch call.method{
            case "scan#startCamera":
                // if self.isCheckFirstTime{
                self.saveLogFile()
                self.startCamera()
                //}
                break;
            case "scan#stopCamera":
                self.stopCamera()
                break;
            case "facecrop":
                
                guard let args = call.arguments else {
                    result("iOS could not recognize flutter arguments in method: (sendParams)")
                    return
                }
                
                if let myArgs = args as? [String: Any]{
                    let cameraimage = myArgs["cameraimage"] as? String
                    let cardfaceimage = myArgs["cardfaceimage"] as? String
                    print("cameraimage :- \(String(describing: cameraimage))")
                    print("cardfaceimage :- \(String(describing: cardfaceimage))")
                    let fileName = String(format: "%@", (cameraimage!.components(separatedBy: "/").last!))
                    print(fileName)
                    
                    self.camaraImage = UIImage(named: cameraimage!)
                    
                    self.clearTempFolder(filename: fileName)
                    
                    if let decodedData = Data(base64Encoded: cardfaceimage!, options: .ignoreUnknownCharacters) {
                        let image = UIImage(data: decodedData)
                        self.faceImage = image
                        //  print(self.faceImage as Any)
                    }
                    
                    
                    
                }
                
                
                if self.camaraImage != nil{
                    var flippedImage: UIImage? = nil
                    if let CGImage = self.camaraImage?.cgImage {
                        flippedImage = UIImage(cgImage: CGImage, scale: self.camaraImage!.scale, orientation: .right)
                    }
                    self.camaraImage = flippedImage!
                    
                    let ratio = CGFloat(self.camaraImage!.size.width) / self.camaraImage!.size.height
                    self.camaraImage = self.compressimage(with: self.camaraImage, convertTo: CGSize(width: 600 * ratio, height: 600))!
                    
                    
                }else{
                    result("")
                }
                
                
                
                let fmInit = EngineWrapper.isEngineInit()
                if !fmInit{
                    
                    /*
                     FaceMatch SDK method initiate SDK engine
                     */
                    
                    EngineWrapper.faceEngineInit()
                }
                
                /*
                 Facematch SDK method to get SDK engine status after initialization
                 Return: -20 = Face Match license key not found, -15 = Face Match license is invalid.
                 */
                let fmValue = EngineWrapper.getEngineInitValue() //get engineWrapper load status
                print(fmValue)
                
                self.faceRegion = nil;
                if (self.faceImage != nil){
                    
                    /*
                     Accura Face SDK method to detect user face from document image
                     Param: Document image
                     Return: User Face
                     */
                    
                    self.faceRegion = EngineWrapper.detectSourceFaces(self.faceImage)
                    print(self.faceRegion as Any)
                }
                
                DispatchQueue.main.asyncAfter(deadline: .now(), execute: {
                    var stFaceImage : String = String()
                    if (self.faceRegion != nil){
                        /*
                         Accura Face SDK method to detect user face from selfie or camera stream
                         Params: User photo, user face found in document scanning
                         Return: User face from user photo
                         */
                        self.face2 = EngineWrapper.detectTargetFaces(self.camaraImage, feature1: self.faceRegion?.feature as Data?)   //identify face in back image which found in front image
                        
                        let data = self.face2?.bound
                        
                        self.imageFace = self.resizeImage(image: self.camaraImage!, targetSize: data!)
                        print(self.imageFace?.size)
                        
                        if (self.imageFace?.size.width)! > 0 && (self.imageFace?.size.height)! > 0 {
                            stFaceImage = self.convertImageToBase64String(img: self.imageFace!)}
                        /*
                         Accura Face SDK method to get face match score
                         Params: face image from document with user image from selfie or camera stream
                         Returns: face match score
                         */
                        
                        result(stFaceImage)
                        
                        
                    } else {
                        result("")
                    }
                    
                })
                
                
                
                
                
                //                print(call.arguments as Any)
                break;
            case "facematch":
                let fm_Score = EngineWrapper.identify(self.faceRegion?.feature, featurebuff2: self.face2?.feature)
                if(fm_Score != 0.0){
                    
                    let twoDecimalPlaces = String(format: "%.2f", fm_Score * 100) //Match score Convert Float Value
                    // todo - add other arg members
                    //                            methodChannel.invokeMethod("onRewardRequest", arguments: args)
                    
                    print(twoDecimalPlaces)
                    result(twoDecimalPlaces)
                }else{
                    result(nil)
                }
                
                
                break;
                
            default:
                break;
            }
        };
        
        imageView = UIImageView(frame: frame)
        imageView.frame = CGRect(x: 0, y: 0, width: view.frame.size.width + (UIScreen.main.bounds.width / 4), height: view.frame.size.height + (UIScreen.main.bounds.height / 4))
        isCheckFirstTime = false
        setImage = true
        
        isFront = true
    }
    
    
    func clearTempFolder(filename: String?) {
        let fileManager = FileManager.default
        let tempFolderPath = NSTemporaryDirectory()
        do {
            let filePaths = try fileManager.contentsOfDirectory(atPath: tempFolderPath)
            for filePath in filePaths {
                print(filePath)
                let fileName = String(format: "%@", (filePath.components(separatedBy: "/").last!))
                if fileName != filename{
                    try fileManager.removeItem(atPath: tempFolderPath + filePath)
                }
            }
        } catch {
            print("Could not clear temp folder: \(error)")
        }
    }
    
    
    func compressimage(with image: UIImage?, convertTo size: CGSize) -> UIImage? {
        UIGraphicsBeginImageContext(size)
        image?.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
        let destImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return destImage
    }
    
    
    required init(coder: NSCoder) {
        super.init(coder: coder)!
    }
    
    
    
    func managePermission(){
        AVCaptureDevice.requestAccess(for: .video) { granted in
            if granted {
                self.isCheckFirstTime = true
                DispatchQueue.main.async {
                    self.imageView.setNeedsLayout()
                    self.imageView.layoutSubviews()
                }
                self.isFront = true
                
                self.setOCRData()
                
                self.ChangedOrientation()
                
                self.videoCameraWrapper?.startCamera()
                
                let shortTap = UITapGestureRecognizer(target: self, action: #selector(self.handleTapToFocus(_:)))
                shortTap.numberOfTapsRequired = 1
                shortTap.numberOfTouchesRequired = 1
                
                
                //   self.startCamera();
            } else {
                print("Not granted access")
            }
        }
    }
    
    func startCamera(){
        // self.isCheckFirstTime = false
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        if status == .authorized {
            self.isCheckFirstTime = true
            DispatchQueue.main.async {
                self.imageView.setNeedsLayout()
                self.imageView.layoutSubviews()
            }
            isFront = true
            
            setOCRData()
            
            ChangedOrientation()
            
            videoCameraWrapper?.startCamera()
            
            let shortTap = UITapGestureRecognizer(target: self, action: #selector(self.handleTapToFocus(_:)))
            shortTap.numberOfTapsRequired = 1
            shortTap.numberOfTouchesRequired = 1
        } else if status == .notDetermined  {
            managePermission();
        }
    }

    func saveLogFile(){

    }

    func stopCamera(){
        videoCameraWrapper?.stopCamera()
        videoCameraWrapper = nil
        imageView.image = nil
    }
    
    
    func setOCRData(){
        dictBackResult.removeAll()
        dictFrontResult.removeAll()
        dictResult.removeAll()
        
        isCheckCard = false
        isCheckcardBack = false
        isCheckCardBackFrint = false
        isflipanimation = false
        
        let filepathAlt = Bundle.main.path(forResource: "haarcascade_frontalface_alt", ofType: "xml")
        
        videoCameraWrapper = VideoCameraWrapper.init(delegate: self, andImageView: imageView, andFacePath: filepathAlt)
        videoCameraWrapper?.saveLogtoLogfile(true)
        imageView.setImageToCenter()
    }
    
    
    
    @objc private func ChangedOrientation() {
        var width: CGFloat = 0.0
        var height: CGFloat = 0.0
        
        width = UIScreen.main.bounds.size.width * 0.90
        height = UIScreen.main.bounds.size.height * 0.30
        
        
        videoCameraWrapper?.changedOrintation(width, height: height)
        
        DispatchQueue.main.async {
            UIView.animate(withDuration: 0.1, delay: 0, options: .curveEaseIn, animations: {
                self.view.layoutIfNeeded()
            }) { _ in
            }
        }
    }
    
    @objc func handleTapToFocus(_ tapGesture: UITapGestureRecognizer?) {
        let acd = AVCaptureDevice.default(for: .video)
        if tapGesture!.state == .ended {
            let thisFocusPoint = tapGesture!.location(in: imageView)
            let focus_x = Double(thisFocusPoint.x / imageView.frame.size.width)
            let focus_y = Double(thisFocusPoint.y / imageView.frame.size.height)
            if acd?.isFocusModeSupported(.autoFocus) ?? false && acd?.isFocusPointOfInterestSupported != nil {
                do {
                    try acd?.lockForConfiguration()
                    
                    if try acd?.lockForConfiguration() != nil {
                        acd?.focusMode = .autoFocus
                        acd?.focusPointOfInterest = CGPoint(x: CGFloat(focus_x), y: CGFloat(focus_y))
                        acd?.unlockForConfiguration()
                    }
                } catch {
                }
            }
        }
        
    }
    
    
}

extension CameraViewController: VideoCameraWrapperDelegate {
    
    public func processedImage(_ image: UIImage!) {
        imageView.image = image
    }
    
    public func onMessage(_ message: String!) {
        let data = NSMutableDictionary()
        data["message"] = message
        
        var dataArray: [NSMutableDictionary] = []
        dataArray.append(data)
        _messagingChannel.sendMessage(dataArray)
    }
    
    public func recognizeFailed(_ message: String!) {
        let data = NSMutableDictionary()
        data["message"] = message
        
        var dataArray: [NSMutableDictionary] = []
        dataArray.append(data)
        _messagingChannel.sendMessage(dataArray)
    }
    
    public func matchedItem(_ image: UIImage!, dict setData: NSMutableDictionary!) {
        if isFront == true{
            imgViewCardFront = image
        }else{
            imgViewCard = image
        }
        dictResult = setData as! [String : String]
        let data = NSMutableDictionary()
        
        if self.isFront!{
            isFront = false
            if !self.isCheckCard{
                self.isCheckCard = true
                self.dictFrontResult.removeAll()
                self.dictFrontResult = self.dictResult
                self.dictResult.removeAll()
                
                AudioServicesPlaySystemSound(1315)
                data["front"] = "done"
                var dataArray: [NSMutableDictionary] = []
                dataArray.append(data)
                _messagingChannel.sendMessage(dataArray)
            }else{
                self.dictResult.removeAll()
            }
        }else{
            if !self.isCheckcardBack{
                self.isCheckcardBack = true
                self.dictBackResult.removeAll()
                self.dictBackResult = self.dictResult
                self.dictResult.removeAll()
                //                self._lblTitle.text! = "Scan Front side of Emirates National ID"
            }
        }
        
        if self.dictFrontResult.count != 0 && self.dictBackResult.count != 0{
            if !self.isCheckCardBackFrint{
                //                self.isCheckCardBackFrint = true
                //                self.videoCameraWrapper?.stopCamera()
                //                imageView.image = nil
                
                if UserDefaults.standard.value(forKey: "ScanningDataMRZ") != nil{
                    dictScanningData  = UserDefaults.standard.value(forKey: "ScanningDataMRZ") as! [String : String]  // Get UserDefaults Store Dictionary
                }
                
                for (key,value) in dictFrontResult{
                    if key.contains("Face_img"){
                        data["face"] = value
                    }
                }
                
                for (key,value) in dictBackResult{
                    if key.contains("_img"){
                        data["_img"] = value
                    }
                    if key.contains("Lines"){
                        data["MRZ"] = value
                    }
                }
                print("dictScanningData.count",dictScanningData.count)
                for (key,value) in dictScanningData{
                    
                    
                    if key.contains("lines"){
                        data["MRZ"] = value
                    }
                    if key.contains("passportType"){
                        data["docType"] = value
                    }
                    if key.contains("surName"){
                        data["surname"] = value
                    }
                    if key.contains("givenNames"){
                        data["givenname"] = value
                    }
                    if key.elementsEqual("passportNumber"){
                        data["docnumber"] = value
                    }
                    if key.contains("passportNumberChecksum"){
                        data["docchecksum"] = value
                    }
                    if key.contains("country"){
                        data["country"] = value
                    }
                    if key.contains("nationality"){
                        data["nationality"] = value
                    }
                    if key.contains("sex"){
                        data["sex"] = value
                    }
                    if key.contains("birth"){
                        
                        //                        let inputFormatter = DateFormatter()
                        //                        inputFormatter.dateFormat = "yymmdd"
                        //                        let showDate = inputFormatter.date(from: value)
                        //                        inputFormatter.dateFormat = "dd-MM-yy"
                        //                        let resultString = inputFormatter.string(from: showDate!)
                        data["birthDate"] = value
                    }
                    if key.contains("BirthChecksum"){
                        data["birthchecksum"] = value
                    }
                    if key.contains("expirationDateChecksum"){
                        data["expirationchecksum"] = value
                    }
                    if key.elementsEqual("expirationDate"){
                        //                        let inputFormatter = DateFormatter()
                        //                        inputFormatter.dateFormat = "yymmdd"
                        //                        let showDate = inputFormatter.date(from: value)
                        //                        inputFormatter.dateFormat = "dd-MM-yy"
                        //                        let resultString = inputFormatter.string(from: showDate!)
                        data["expiryDate"] = value
                    }
                    if key.elementsEqual("personalNumber"){
                        data["otherid"] = value
                    }
                    if key.contains("personalNumberChecksum"){
                        data["otheridchecksum"] = value
                    }
                    if key.contains("secondRowChecksum"){
                        data["secondrowchecksum"] = value
                    }
                    
                    if key.contains("retval"){
                        var result=""
                        if value.contains("0") {
                            result = "Failed"
                        } else if value.contains("1"){
                            result = "Correct Mrz"
                        } else if value.contains("2") {
                            result = "Incorrect Mrz"
                        }
                        data["Result"] = result
                        
                        
                        
                    }
                }
                data["frontBitmap"] = convertImageToBase64String(img: imgViewCardFront!)
                
                data["BackImage"] = convertImageToBase64String(img: imgViewCard!)
                
                //                dictBackResult11.append(data as! [String : String])
                //                dictBackResult11.append(dictBackResult)
                //                dictBackResult11.append(dictScanningData)
                print("data.count    :",data.count)
                var dataArray: [NSMutableDictionary] = []
                dataArray.append(data)
                AudioServicesPlaySystemSound(1315)
                print("dataArray.count    :",dataArray)
                _messagingChannel.sendMessage(dataArray)
                
                
            }
        }else{
            if !self.isflipanimation!{
                self.isflipanimation = true
                return
            }else{
                return
            }
        }
        
    }
    
    
    func resizeImage(image: UIImage, targetSize: CGRect) -> UIImage {
        let contextImage: UIImage = UIImage(cgImage: image.cgImage!)
        var newX = targetSize.origin.x - (targetSize.size.width * 0.4)
        var newY = targetSize.origin.y - (targetSize.size.height * 0.4)
        var newWidth = targetSize.size.width * 1.8
        var newHeight = targetSize.size.height * 1.8
        var image1 :UIImage=UIImage()
        if newX < 0 {
            newX = 0
        }
        if newY < 0 {
            newY = 0
        }
        if newX + newWidth > image.size.width{
            newWidth = image.size.width - newX
        }
        if newY + newHeight > image.size.height{
            newHeight = image.size.height - newY
        }
        // This is the rect that we've calculated out and this is what is actually used below
        let rect = CGRect(x: newX, y: newY, width: newWidth, height: newHeight)
        if rect.width > 0 && rect.height > 0 {
            let imageRef: CGImage = contextImage.cgImage!.cropping(to: rect)!
            image1 = UIImage(cgImage: imageRef)
        }
        return image1
    }
    
    
    
    func convertImageToBase64String (img: UIImage) -> String {
        let imageData:NSData = img.jpegData(compressionQuality: 0.50)! as NSData //UIImagePNGRepresentation(img)
        let imgString = imageData.base64EncodedString(options: .init(rawValue: 0))
        return imgString
    }
    
}

extension UIImageView {
    func setImageToCenter() {
        let imageSize = self.image?.size
        self.sizeThatFits(imageSize ?? CGSize.zero)
        var imageViewCenter = self.center
        imageViewCenter.x = self.frame.midX
        self.center = imageViewCenter
    }
}
