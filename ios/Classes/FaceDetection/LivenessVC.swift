

import UIKit
import AVFoundation
//import Firebase
//import SVProgressHUD

public protocol LivenessData {
    func LivenessData(stLivenessValue: String, livenessImage: UIImage, status: Bool)
}

//import SDWebImage
class LivenessVC: UIViewController {
    
    @IBOutlet weak var _imageView: UIImageView!
    @IBOutlet weak var _lblTitle: UILabel!
    @IBOutlet weak var _constant_height: NSLayoutConstraint!
    @IBOutlet weak var _constant_width: NSLayoutConstraint!
    
    @IBOutlet weak var imageLeading: NSLayoutConstraint!
    @IBOutlet weak var lblOCRMsg: UILabel!
    @IBOutlet weak var lblTitleCountryName: UILabel!
    @IBOutlet weak var constraintFlipImageWidth: NSLayoutConstraint!
    @IBOutlet weak var constraintFlipImageHeight: NSLayoutConstraint!
    @IBOutlet weak var viewNavigation: UIView!
    @IBOutlet var viewLiveness: UIView!
    
    @IBOutlet weak var viewTitleLable: UIView!
    
    @IBOutlet weak var labelTitle: UILabel!
    
    @IBOutlet weak var buttonBack: UIButton!
    @IBOutlet weak var viewLogo: UIView!
    
    @IBOutlet weak var viewSetLOGO: UIView!
    var videoCameraWrapper: VideoCameraWrapper? = nil
    var delegate: LivenessData?
    var frameSublayer = CALayer()
    
    //MARK:- Variable

    var isCheckCard : Bool = false

    var isCheckFirstTime : Bool?
    var setImage : Bool?
    var isCheckFace: Bool = false
    var feedBackframeMessage: String?
    var feedBackAwayMessage: String?
    var feedBackOpenEyesMessage: String?
    var feedBackCloserMessage: String?
    var feedBackCenterMessage: String?
    var feedBackMultipleFaceMessage: String?
    var feedBackFaceSteadymessage: String?
    var feedBackLowLightMessage: String?
    var feedBackBlurFaceMessage: String?
    var feedBackGlareFaceMessage: String?
    var livenessURL: String?
    var ischeckoneytime: Bool?
    var ischeckLiveness: Bool?
    
//        // MARK: - ML Kit Vision Property
//        lazy var vision = Vision.vision()
//        lazy var faceDetector: VisionFaceDetector = { () -> VisionFaceDetector in
//            // Real-time contour detection of multiple faces
//            let options = VisionFaceDetectorOptions()
//            options.performanceMode = .accurate
//            options.contourMode = .all
//            options.landmarkMode = .all
//            options.classificationMode = .all
//            
//            return vision.faceDetector(options: options)
//        }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        isCheckFirstTime = false
        setImage = true
       // imageViewCountryImage.layer.cornerRadius = 8.0
        
        // Do any additional setup after loading the view.
        
        viewTitleLable.setShadowToView()
        
        NotificationCenter.default.addObserver(self, selector: #selector(ChangedOrientation), name: UIDevice.orientationDidChangeNotification, object: nil)
        var width : CGFloat = 0
        var height : CGFloat = 0
        
        width = UIScreen.main.bounds.size.width
        height = UIScreen.main.bounds.size.height
        width = width * 0.90
        height = height * 0.30

        _constant_width.constant = width
        _constant_height.constant = height
        
        
         let width11 = (UIScreen.main.bounds.width / 1.7);
         let height11 = (width11 * 1.4);
         let x = (UIScreen.main.bounds.width / 2) - 35
         let y = ((UIScreen.main.bounds.height / 2) - 35) + ((height11 / 2) - 25)
        
        
        let backgroundView = CustomView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height))
        backgroundView.backgroundColor = UIColor.clear
        
        let button = UIButton(frame: CGRect(x: (view.bounds.width - 32), y: (self.topLayoutGuide.length + 32), width: 20, height: 20))
        button.addTarget(self, action: #selector(buttonAction), for: .touchUpInside)
        let image = UIImage(named: "icn_close")
        button.setImage(UIImage(named: "icn_close"), for: .normal)
        view.addSubview(backgroundView)
        backgroundView.addSubview(_imageView)
        view.addSubview(button)
        backgroundView.addSubview(viewLogo)
        
        let drawingView = UIView.init(frame: CGRect(x: x, y: y, width: 70, height: 70))
        drawingView.backgroundColor = .clear
        backgroundView.addSubview(drawingView)
        let drawingImageView = UIImageView.init(frame: CGRect(x: 0, y: 0, width: 70, height: 70))
        drawingImageView.image = UIImage(named: "accuraScanLogo")
        drawingImageView.contentMode = .scaleAspectFit
        drawingView.addSubview(drawingImageView)
        
        let drawingLabel = UILabel.init(frame: CGRect(x: 0, y: 5, width: 70, height: 15))
        drawingLabel.textColor = .white
        drawingLabel.font = drawingLabel.font.withSize(10)
        drawingLabel.text = ""
        
        drawingLabel.textAlignment = .center
        drawingView.addSubview(drawingLabel)
        
        
        
        let backGroundClr = UserDefaultLiveness.getLivenessData(key: "backGroundColor")
        
        if backGroundClr != ""{
            viewLiveness.backgroundColor = UserDefaultLiveness.colorWithHexString(hexString: backGroundClr)
        }else{
            viewLiveness.backgroundColor = UIColor.lightGray
        }

        
//        let image = UIImage(named: "icn_close")
//
//        button.setImage(image, for: .normal)
        let closeIconClr = UserDefaultLiveness.getLivenessData(key: "closeIconColor")
        
        if closeIconClr != ""{
            button.tintColor = UserDefaultLiveness.colorWithHexString(hexString: closeIconClr)
        }else{
            
            button.tintColor = UIColor.black
        }
        
        let feedbackBackGroundClr = UserDefaultLiveness.getLivenessData(key: "feedbackBackGroundColor")
        
        if feedbackBackGroundClr != ""{
            viewTitleLable.backgroundColor = UserDefaultLiveness.colorWithHexString(hexString: feedbackBackGroundClr)
        }else{
            viewTitleLable.backgroundColor = UIColor.lightGray
        }
        
        let feedbackTextClr = UserDefaultLiveness.getLivenessData(key: "feedbackTextColor")
        
        if feedbackTextClr != ""{
            labelTitle.textColor = UserDefaultLiveness.colorWithHexString(hexString: feedbackTextClr)
        }else{
            labelTitle.textColor = UIColor.black
        }
        
        let feedbackTextSize = UserDefaultLiveness.getLivenessData(key: "setFeedbackTextSize")
        let floatVal  = (feedbackTextSize as NSString).floatValue //Now converted to float
        if feedbackTextSize != ""{
            labelTitle.font = labelTitle.font.withSize(CGFloat(floatVal))
        }else{
            labelTitle.font = labelTitle.font.withSize(18)
        }
        
        
        var feedBackframeMsg = UserDefaultLiveness.getLivenessData(key: "feedBackframeMessage")
        
        if feedBackframeMsg != ""{
            labelTitle.text = feedBackframeMsg
            feedBackframeMessage = feedBackframeMsg
        }else{
            labelTitle.text = "Frame Your Face"
            feedBackframeMessage = "Frame Your Face"
        }
        
        
        let feedBackAwayMsg = UserDefaultLiveness.getLivenessData(key: "feedBackAwayMessage")
        
        if feedBackAwayMsg != ""{
            feedBackAwayMessage = feedBackAwayMsg
        }else{
            feedBackAwayMessage = "Move Phone Away"
        }
        
        let feedBackOpenEyesMsg = UserDefaultLiveness.getLivenessData(key: "feedBackOpenEyesMessage")
        
        if feedBackOpenEyesMsg != ""{
            feedBackOpenEyesMessage = feedBackOpenEyesMsg
        }else{
            feedBackOpenEyesMessage = "Keep Your Eyes Open"
        }
        
        let feedBackCloserMsg = UserDefaultLiveness.getLivenessData(key: "feedBackCloserMessage")
        
        if feedBackCloserMsg != ""{
            feedBackCloserMessage = feedBackCloserMsg
        }else{
            feedBackCloserMessage = "Move Phone Closer"
        }
        
        let feedBackCenterMsg = UserDefaultLiveness.getLivenessData(key: "feedBackCenterMessage")
        
        if feedBackCenterMsg != ""{
            feedBackCenterMessage = feedBackCenterMsg
        }else{
            feedBackCenterMessage = "Move Phone Center"
        }
        
        let livenessUrl = UserDefaultLiveness.getLivenessData(key: "livenessURL")
        
        if livenessUrl != ""{
            livenessURL = "\(livenessUrl)"
        }else{
            livenessURL = ""
        }
        
        let feedbackMultipleFaceMessage = UserDefaultLiveness.getLivenessData(key: "feedBackMultipleFaceMessage")
        
        if feedbackMultipleFaceMessage != ""{
            feedBackMultipleFaceMessage = feedbackMultipleFaceMessage
        }else{
            feedBackMultipleFaceMessage = "Multiple face detected"
        }
        
        let feedbackFaceSteady = UserDefaultLiveness.getLivenessData(key: "feedBackFaceSteadymessage")
        
        if feedbackFaceSteady != ""{
            feedBackFaceSteadymessage = feedbackFaceSteady
        }else{
            feedBackFaceSteadymessage = "Keep Your Head Steady"
        }
        
        let feedbackLowLightMessage = UserDefaultLiveness.getLivenessData(key: "feedBackLowLightMessage")
        
        if feedbackLowLightMessage != ""{
            feedBackLowLightMessage = feedbackLowLightMessage
        }else{
            feedBackLowLightMessage = "Low light detected"
        }
        
        let feedbackFaceBlurMessage = UserDefaultLiveness.getLivenessData(key: "feedBackBlurFaceMessage")
        
        if feedbackFaceBlurMessage != ""{
            feedBackBlurFaceMessage = feedbackFaceBlurMessage
        }else{
            feedBackBlurFaceMessage = "Blur detected over face"
        }
        
        let feedbackGlareMessage = UserDefaultLiveness.getLivenessData(key: "feedBackGlareFaceMessage")
        
        if feedbackGlareMessage != ""{
            feedBackGlareFaceMessage = feedbackGlareMessage
        }else{
            feedBackGlareFaceMessage = "Blur detected over face"
        }


        button.isUserInteractionEnabled = true
        backgroundView.isUserInteractionEnabled = true
        var rootViewController = UIApplication.shared.keyWindow?.rootViewController
        if let navigationController = rootViewController as? UINavigationController {
            rootViewController = navigationController.viewControllers.first
        }
        if let tabBarController = rootViewController as? UITabBarController {
            rootViewController = tabBarController.selectedViewController
        }
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        
        if status == .authorized {
            setOCRData()
            isCheckFirstTime = true
        } else if status == .denied {
            let alert = UIAlertController(title: "AccuraSdk", message: "It looks like your privacy settings are preventing us from accessing your camera.", preferredStyle: .alert)
            let yesButton = UIAlertAction(title: "OK", style: .default) { _ in
                if #available(iOS 10.0, *) {
                    UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!, options: [:], completionHandler: nil)
                } else {
                    UIApplication.shared.openURL(URL(string: UIApplication.openSettingsURLString)!)
                }
            }
            alert.addAction(yesButton)
            
            self.present(alert, animated: true, completion: nil)
        } else if status == .restricted {
            
        } else if status == .notDetermined  {
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    self.isCheckFirstTime = true
                    DispatchQueue.main.async {
                    self._imageView.setNeedsLayout()
                    self._imageView.layoutSubviews()
                    }
                    self.setOCRData()
                    
                    self.ChangedOrientation()
                    
                    self.videoCameraWrapper?.startCamera()
                } else {
                    print("Not granted access")
                }
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        ischeckoneytime = true
        isCheckCard = true
        if setImage!{
        setImage = false
        _imageView.setNeedsLayout()
        _imageView.layoutSubviews()
        }
        
        if isCheckFirstTime!{
        isCheckFirstTime = true
        self.ChangedOrientation()
        
        if videoCameraWrapper == nil {
            setOCRData()
        }
            
         videoCameraWrapper?.startCamera()
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        
        UserDefaults.standard.removeObject(forKey: "backGroundColor")
        UserDefaults.standard.removeObject(forKey: "closeIconColor")
        UserDefaults.standard.removeObject(forKey: "feedbackBackGroundColor")
        UserDefaults.standard.removeObject(forKey: "feedbackTextColor")
        UserDefaults.standard.removeObject(forKey: "setFeedbackTextSize")
        UserDefaults.standard.removeObject(forKey: "feedBackframeMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackAwayMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackCloserMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackCenterMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackMultipleFaceMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackFaceSteadymessage")
        UserDefaults.standard.removeObject(forKey: "feedBackLowLightMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackBlurFaceMessage")
        UserDefaults.standard.removeObject(forKey: "feedBackGlareFaceMessage")
//        UserDefaults.standard.removeObject(forKey: "livenessURL")
        UserDefaults.standard.synchronize()
        
        
        videoCameraWrapper?.stopCamera()
        videoCameraWrapper = nil
        _imageView.image = nil
        super.viewWillDisappear(animated)
    }

    //MARK:- Other Method
    func setOCRData(){
        videoCameraWrapper = VideoCameraWrapper.init(delegate: self, andImageView: _imageView, andMsgLabel: labelTitle, andfeedBackframeMessage: feedBackframeMessage, andfeedBackAwayMessage: feedBackAwayMessage, andfeedBackOpenEyesMessage: feedBackOpenEyesMessage, andfeedBackCloserMessage: feedBackCloserMessage, andfeedBackCenterMessage: feedBackCenterMessage, andfeedBackMultipleFaceMessage: feedBackMultipleFaceMessage, andfeedBackFaceSteady: feedBackFaceSteadymessage, andfeedBackLowLightMessage: feedBackLowLightMessage, andfeedBackBlurFaceMessage: feedBackBlurFaceMessage, andfeedBackGlareFaceMessage: feedBackGlareFaceMessage, andcheckLivess: true)//init(delegate: self, andImageView: _imageView, andMsgLabel: labelTitle, andfeedBackframeMessage:feedBackframeMessage, andfeedBackAwayMessage: feedBackAwayMessage, andfeedBackOpenEyesMessage: feedBackOpenEyesMessage, andfeedBackCloserMessage: feedBackCloserMessage, andfeedBackCenterMessage: feedBackCenterMessage, andcheckLivess: true)
    }

    @objc private func ChangedOrientation() {
        var width: CGFloat = 0.0
        var height: CGFloat = 0.0
        
        width = UIScreen.main.bounds.size.width / 1.7
        height = UIScreen.main.bounds.size.height / 1.7

        
        
        
        DispatchQueue.main.async {
            self._constant_width.constant = width
            self._constant_height.constant = height
           UIView.animate(withDuration: 0.1, delay: 0, options: .curveEaseIn, animations: {
               self.view.layoutIfNeeded()
           }) { _ in
            }
        }
    }
    
    
    @IBAction func buttonBackAction(_ sender: UIButton) {
//        videoCameraWrapper?.stopCamera()
//        self.dismiss(animated: true, completion: nil)
    }
    
    @objc func buttonAction(sender: UIButton!) {
//      videoCameraWrapper?.stopCamera()
      self.dismiss(animated: true, completion: nil)
    }
    
    
    
    private func removeFrames() {
        guard let sublayers = frameSublayer.sublayers else { return }
        for sublayer in sublayers {
            sublayer.removeFromSuperlayer()
        }
    }
    
    func drawRectangleOnImage(image: UIImage, rect: CGRect, color: UIColor) -> UIImage {
        let imageSize = image.size
        let scale: CGFloat = 0
        UIGraphicsBeginImageContextWithOptions(imageSize, false, scale)
        let context = UIGraphicsGetCurrentContext()

        image.draw(at: CGPoint.zero)

        let rectangle = CGRect(x: rect.origin.x, y: rect.origin.y, width: rect.size.width, height: rect.size.height)

        context!.setFillColor(UIColor.clear.cgColor)
        context?.setStrokeColor(color.cgColor)
        context!.setLineWidth(2.0)
        context!.addRect(rectangle)
        context!.drawPath(using: .stroke)

        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return newImage!
    }
    
    
    func cropToBounds(image: UIImage, width: Double, height: Double) -> UIImage {

        let cgimage = image.cgImage!
        let contextImage: UIImage = UIImage(cgImage: cgimage)
        let contextSize: CGSize = contextImage.size
        var posX: CGFloat = 0.0
        var posY: CGFloat = 0.0
        var cgwidth: CGFloat = CGFloat(width)
        var cgheight: CGFloat = CGFloat(height)

        // See what size is longer and create the center off of that
        if contextSize.width > contextSize.height {
            posX = ((contextSize.width - contextSize.height) / 2)
            posY = 0
            cgwidth = contextSize.height
            cgheight = contextSize.height
        } else {
            posX = 0
            posY = ((contextSize.height - contextSize.width) / 2)
            cgwidth = contextSize.width
            cgheight = contextSize.width
        }

        let rect: CGRect = CGRect(x: posX, y: posY, width: cgwidth, height: cgheight)

        // Create bitmap image from context using the rect
        let imageRef: CGImage = cgimage.cropping(to: rect)!

        // Create a new image based on the imageRef and rotate back to the original orientation
        let image: UIImage = UIImage(cgImage: imageRef, scale: image.scale, orientation: image.imageOrientation)

        return image
    }
}

extension UIView {
    func setShadowToView() {
        self.layer.cornerRadius = 8.0
        self.layer.borderColor = UIColor.black.cgColor
        self.layer.borderWidth = 0.0
        self.layer.shadowColor = UIColor.black.cgColor
        self.layer.shadowOpacity = 0.5
        self.layer.shadowRadius = 3.0
        self.layer.shadowOffset = CGSize(width: 0, height: 5.0)
    }
}

extension LivenessVC: VideoCameraWrapperDelegate {
    
    func livenessData(_ livenessImage: UIImage!, andshowImage showImage: UIImage!) {

        labelTitle.text = "Processing..."
        var dicData : [String: AnyObject] = [String: AnyObject]()
        var intData: Double = 0
        var status: Bool?
        var showImageCom: UIImage?
       let showImageData = showImage.jpegData(compressionQuality: 1.0)
        if showImageData != nil{
          showImageCom = UIImage(data: showImageData!)
           showImageCom = showImageCom?.resizeCI(size: showImageCom!.size)
            
        }else{
            showImageCom = UIImage(named: "default_user")
        }
//        SVProgressHUD.show(withStatus: "Loading...")
            if (self.ischeckoneytime!){
                self.ischeckoneytime = false
                
                self.delegate?.LivenessData(stLivenessValue: "0.00 %", livenessImage: showImageCom!, status: true)
            }
//            SVProgressHUD.dismiss()
//        }
        
          self.dismiss(animated: true, completion: nil)

        }

    
    func processedImage(_ image: UIImage!) {
        _imageView.image = image
    }
    
         func resizeImage(image: UIImage) -> UIImage {
            
            //setup path for mask and border
            let width11 = (image.size.width / 1.7);
             let height11 = (width11 * 1.4);
             let x = (image.size.width / 2) - (width11 / 2)
            let y = (image.size.height / 2) - (height11 / 2)
            
            let contextImage: UIImage = UIImage(cgImage: image.cgImage!)

            // This is the rect that we've calculated out and this is what is actually used below
            let rect = CGRect(x: x, y: y, width: width11, height: height11)
            let imageRef: CGImage = contextImage.cgImage!.cropping(to: rect)!
            
            let image1: UIImage = UIImage(cgImage: imageRef)
            return image1
        }
    
//    func compressimage(with image: UIImage?, convertTo size: CGSize) -> UIImage? {
//        UIGraphicsBeginImageContext(size)
//        image?.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
//        let destImage = UIGraphicsGetImageFromCurrentImageContext()
//        UIGraphicsEndImageContext()
//        return destImage
//    }
    
    
}


class CustomView: UIView {
override func draw(_ rect: CGRect) {
    super.draw(rect)
    self.backgroundColor = UIColor.clear

    //setup path for mask and border
    let width11 = (UIScreen.main.bounds.width / 1.7);
     let height11 = (width11 * 1.4);
     let x = (UIScreen.main.bounds.width / 2) - (width11 / 2)
     let y = (UIScreen.main.bounds.height / 2) - (height11 / 2)
     
    let maskPath = UIBezierPath(ovalIn: CGRect(x: x, y: y, width: width11, height: height11))
    
    

    //setup MASK
    self.layer.mask = nil;
    let maskLayer = CAShapeLayer()
    maskLayer.frame = self.bounds;
    maskLayer.path = maskPath.cgPath
    self.layer.mask = maskLayer

    //setup Border for Mask
    let borderLayer = CAShapeLayer()
    borderLayer.path = maskPath.cgPath
    borderLayer.lineWidth = 3
    borderLayer.strokeColor = UIColor.white.cgColor
    borderLayer.fillColor = UIColor.clear.cgColor
    borderLayer.frame = self.bounds
    self.layer.addSublayer(borderLayer)
    

      
}
}

class Draw: UIView {

    override init(frame: CGRect) {
        super.init(frame: frame)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func draw(_ rect: CGRect) {
        let h = rect.height
        let w = rect.width
        let color:UIColor = UIColor.yellow

        let drect = CGRect(x: rect.origin.x,y: rect.origin.y,width: w,height: h)
        let bpath:UIBezierPath = UIBezierPath(rect: drect)

        color.set()
        //bpath.stroke()
        let borderLayer = CAShapeLayer()
        borderLayer.path = bpath.cgPath
        borderLayer.lineWidth = 3
        borderLayer.strokeColor = UIColor.white.cgColor
        borderLayer.fillColor = UIColor.clear.cgColor
        borderLayer.frame = self.bounds
        self.layer.addSublayer(borderLayer)

        print("it ran")

        NSLog("drawRect has updated the view")

    }

}


extension UIImage {
    func resizeCI(size:CGSize) -> UIImage? {
        let scale = (Double)(size.width) / (Double)(self.size.width)
        let image = UIKit.CIImage(cgImage:self.cgImage!)
            
            let filter = CIFilter(name: "CILanczosScaleTransform")!
            filter.setValue(image, forKey: kCIInputImageKey)
        filter.setValue(NSNumber(value:scale), forKey: kCIInputScaleKey)
            filter.setValue(1.0, forKey:kCIInputAspectRatioKey)
        let outputImage = filter.value(forKey: kCIOutputImageKey) as! UIKit.CIImage
            
        let context = CIContext(options: [CIContextOption.useSoftwareRenderer: false])
        let resizedImage = UIImage(cgImage: context.createCGImage(outputImage, from: outputImage.extent)!)
            return resizedImage
    }
}
