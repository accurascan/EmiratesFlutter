//
//  VideoCameraWrapper.m
//  AccuraSDK
//
//  Created by Chang Alex on 1/26/20.
//  Copyright © 2020 Elite Development LLC. All rights reserved.
//

#import "VideoCameraWrapper.h"
#import "opencv2/highgui/ios.h"
#include <opencv2/imgproc/imgproc.hpp>
#import "GlobalMethods.h"
#include "Accura.h"
#include "zinterface.mm"
#include "faceengine.h"

@interface VideoCameraWrapper() <CvVideoCameraDelegate>
@end


@implementation VideoCameraWrapper
{
    CvVideoCamera *videoCamera;
    cv::Mat _matOrg;
    cv::Mat _livOrg;
}

NSLock *lock1 = [[NSLock alloc]init];

CGFloat viewScanningLayerWidth = 0.0;
CGFloat viewScanningLayerHeight = 0.0;
CGFloat scanningImgeHeightMultipler =  0.0;
CGFloat scanningImgeWidthMultipler = 0.0;
CGFloat navigationHeight = 0.0;
CGFloat scanningOriginX = 0.0;

bool threadrunning = NO;

RecType recType = REC_INIT;
bool bRecDone = false;
bool bFaceReplace = false;
bool bMrzFirst = false;

int retval = 0;
int retface = 0;
int reccnt = 0;

int ret = -1;

NSString* lines = @"";
NSString* mrzLines = @"";

bool success;
NSString* passportType = @"";
NSString* country = @"";
NSString* surName = @"";
NSString* otherID = @"";

NSString* givenNames = @"";
NSString* passportNumber = @"";
NSString* passportNumberChecksum = @"";
NSString* nationality = @"";
NSString* birth = @"";
NSString* BirthChecksum = @"";
NSString* sex = @"";
NSString* expirationDate = @"";
NSString* expirationDateChecksum = @"";
NSString* personalNumber = @"";
NSString* personalNumberChecksum = @"";
NSString* secondRowChecksum = @"";
NSString* placeOfBirth = @"";
NSString* docSum = @"";
NSString* placeOfIssue = @"";
UIImage* photoImage = nil;
UIImage* documentImage = nil; //mrz document image
UIImage* docfrontImage = nil; //front document image


bool isCrrountCard;
UIImageView* docfrontImg; //front document image
UIImageView * setFrameImage; //Back Document image
nlohmann::json wholeresponce;
BOOL isBack;
BOOL isFront;

cv::Mat grayscaleMat;

int backSide;
int frontSide;
int cardPosition;
cv::Mat firstTemp;

cv::Mat gimg;

UIImage *cropImage;
UIImage *cropImage1;
std::map<int, Data_t> finaldata;

NSArray *stData;

string changeCard;
BOOL isMRZ;

cv::Mat img;

UIImageView *imageView;

NSString* facePath = @"";
string facePathData;

BOOL isFirstMRZ;

NSTimer *timer;
string docMsgCard;
string docMsgCard1;

dispatch_time_t popTime;
double delayInSeconds = 1.0;

BOOL isCheckMSG;
BOOL isFirstMSG;

BOOL isHologram;
BOOL ischeckMation;
NSString* feedBackframeMessage1;
NSString* feedBackAwayMessage1;
NSString* feedBackOpenEyesMessage1;
NSString* feedBackCloserMessage1;
NSString* feedBackCenterMessage1;
NSString* feedBackMultipleFaceMessage1;
NSString* feedBackFaceSteadyMessage1;
NSString* feedBackLowLightMessage1;
NSString* feedBackBlurFaceMessage1;
NSString* feedBackGlareFaceMessage1;
bool ischekLivenss;
BOOL inProcessing;
UILabel *feedbackLivenessMSG;
BOOL isCheckFaceCalled;
bool isCheckFaceCount;
NSString *livenessStatus;
bool isShowlivenessMSG = false;
bool inProcessingLiveness = false;

-(id)initWithDelegate:(UIViewController<VideoCameraWrapperDelegate>*)delegate andImageView:(UIImageView *)iv andMsgLabel:(UILabel*)l andfeedBackframeMessage:(NSString*)feedBackframeMessage andfeedBackAwayMessage:(NSString*)feedBackAwayMessage andfeedBackOpenEyesMessage:(NSString*)feedBackOpenEyesMessage andfeedBackCloserMessage:(NSString*)feedBackCloserMessage andfeedBackCenterMessage:(NSString*)feedBackCenterMessage andfeedBackMultipleFaceMessage:(NSString*)feedBackMultipleFaceMessage andfeedBackFaceSteady:(NSString*)feedBackFaceSteady andfeedBackLowLightMessage:(NSString*)feedBackLowLightMessage andfeedBackBlurFaceMessage:(NSString*)feedBackBlurFaceMessage andfeedBackGlareFaceMessage:(NSString*)feedBackGlareFaceMessage andcheckLivess:(bool)checkLivenss
{

    feedBackframeMessage1= feedBackframeMessage;
    feedBackAwayMessage1 = feedBackAwayMessage;
    feedBackOpenEyesMessage1 = feedBackOpenEyesMessage;
    feedBackCloserMessage1 = feedBackCloserMessage;
    feedBackCenterMessage1 = feedBackCenterMessage;
    feedBackMultipleFaceMessage1 = feedBackMultipleFaceMessage;
    feedBackFaceSteadyMessage1 = feedBackFaceSteady;
    feedBackLowLightMessage1 = feedBackLowLightMessage;
    feedBackBlurFaceMessage1 = feedBackBlurFaceMessage;
    feedBackGlareFaceMessage1 = feedBackGlareFaceMessage;

    ischekLivenss = true;
    inProcessing = false;
    imageView = iv;
    feedbackLivenessMSG = l;

    isCheckFaceCalled = true;
    isCheckFaceCount = true;
    //    options = [[MLKFaceDetectorOptions alloc] init];
    //    options.performanceMode = MLKFaceDetectorPerformanceModeAccurate;
    //    options.landmarkMode = MLKFaceDetectorLandmarkModeAll;
    //    options.classificationMode = MLKFaceDetectorClassificationModeAll;

    //    vision = [FIRVision vision];
    //    faceDetector = [MLKFaceDetector faceDetectorWithOptions:options];

    if(ischekLivenss){
        isShowlivenessMSG = true;
        livenessStatus = @"Frame your face";
        [self blink:0];
    }
    if (self = [super init]) {
        self.delegate = delegate;

        videoCamera = [[CvVideoCamera alloc] init];
        // videoCamera = [[CvVideoCamera alloc] initWithParentView:imageView];
        videoCamera.delegate = self;
        videoCamera.defaultAVCaptureDevicePosition = AVCaptureDevicePositionFront;
        videoCamera.defaultAVCaptureSessionPreset = AVCaptureSessionPreset1280x720;
        videoCamera.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationPortrait;
        videoCamera.defaultFPS = 30;
        videoCamera.grayscaleMode = NO;
        videoCamera.rotateVideo = NO;
        //        [NSTimer scheduledTimerWithTimeInterval:3.0
        //           target:self
        //           selector:@selector(timer_Tick:)
        //           userInfo:nil
        //           repeats:NO];
    }
    return self;
}

-(void)drawFeaturesForFace:(UIImage *)image11{
    if(isCheckFaceCalled) {

        //                            UIImage* newCropImage = [self cropImage:image11];
                                    //                             CGFloat imagewidth11 = newCropImage.size.width;
                                    //                            CGFloat imageheight11 = newCropImage.size.height;
                                    //                            NSLog(@"imagewidth11 %f",imagewidth11);
                                    //                            NSLog(@"imageheight11 %f",imageheight11);
                                    //                            UIImage* newCropImage1 = [self cropToBounds:newCropImage :imageheight11 :imageheight11];
                                    CGFloat width11 = (image11.size.width / 1.7);
                                    CGFloat height11 = (width11 * 1.4);
                                    CGFloat x = (image11.size.width / 2) - (width11 / 2);
                                    CGFloat y = (image11.size.height / 2) - (height11 / 2);

                                    CGRect rect = CGRectMake(x, y, width11, height11);
//                                    UIImage* newCropImage = [self cropImage:image11 cropToRect:rect];//crop according to oval rectangle
                                    //                            NSLog(@"rect::%@", NSStringFromCGRect(rect));
                                    CGFloat wX = rect.size.width * 0.15;
                                    CGFloat wY = rect.size.height * 0.15;
                                    CGFloat left = rect.origin.x - wX;
                                    CGFloat top = rect.origin.y - wY;
                                    CGFloat width = rect.size.width + (wX * 2);
                                    CGFloat height = rect.size.height + (wY * 2);
        CGRect extendOval = CGRectMake(left, top, width, height);
//            cv::Mat brighter = cvMatFromUIImage(image11) - cvScalar(80, 80, 80);

        NSString* pathModel2 = [[NSBundle mainBundle] pathForResource:@"haarcascade_frontalface_alt" ofType:@"xml"];



        int ret1 = accurascan_facedetection_facedetectionutils_FaceDetectionProcessor_initEngine([pathModel2 UTF8String], -1, -1, -1, -1);
//            accurascan_facedetection_facedetectionutils_FaceDetectionProcessor_initEngine( [pathModel2 UTF8String], -1, livenessBlur, livenessGlareMin, livenessGlareMax);
        cv::Mat imgg = cvMatFromUIImage(image11);
        int ret2  = checklivenessValidation(imgg, 90, -1, -1, -1);
//                            NSLog(@"liveness:- %d, %d, %d", livenessBlur,livenessGlareMin, livenessGlareMax);
//            double defaultBlur = 30.00*3.00;
//                                [self saveLogToFile:[NSString stringWithFormat:@"B - %d",ret]];
        if(ret2 == -3){
            [self reco_msg_Liveness:feedBackGlareFaceMessage1];
            isCheckFaceCount = true;
            return ;
        }
        if(ret2 == -2){
            [self reco_msg_Liveness:feedBackBlurFaceMessage1];
            isCheckFaceCount = true;
            return;
        }
        int pint[4];

//        isCheckFaceCount = true;
//        return;

        int ret = accurascan_facedetection_facedetectionutils_FaceDetectionProcessor_detectFace(imgg, [[NSNumber numberWithFloat:left] intValue], [[NSNumber numberWithFloat:top] intValue], [[NSNumber numberWithFloat:width] intValue], [[NSNumber numberWithFloat:height] intValue],pint);

            if(ret == -7) {
                //                                doucumentMsg.text = feedBackCenterMessage1;
                [self reco_msg_Liveness:feedBackCenterMessage1];
                isCheckFaceCount = true;
    //            [self saveLogToFile:[NSString stringWithFormat:@"Ce - %d  %d",(-10.0 >= face.headEulerAngleY),(face.headEulerAngleY >= 10)]];
                return;

            } else if (ret == -4) {
                [self reco_msg_Liveness:feedBackOpenEyesMessage1];
                isCheckFaceCount = true;
    //            [self saveLogToFile:[NSString stringWithFormat:@"O - %d  %d",(face.rightEyeOpenProbability < 0.8?1:0),(face.rightEyeOpenProbability < 0.8?1:0)]];
                return;

            } else if (ret == -3) {
                [self reco_msg_Liveness:feedBackAwayMessage1];
                isCheckFaceCount = true;
    //            [self saveLogToFile:[NSString stringWithFormat:@"A - %d  %d %d %d",(frame.origin.x < extendOval.origin.x?1:0),(frame.origin.y < extendOval.origin.y?1:0),((frame.size.width + frame.origin.x) > (extendOval.size.width + extendOval.origin.x)?1:0),((frame.size.height + frame.origin.y) > (extendOval.size.height + extendOval.origin.y)?1:0)]];
                return;
            } else if (ret == -2) {
                [self reco_msg_Liveness:feedBackCloserMessage1];
                isCheckFaceCount = true;
    //            [self saveLogToFile:[NSString stringWithFormat:@"C - %d  %d %d %d",(frame.origin.x > insetOval.origin.x),(frame.origin.y > insetOval.origin.y),((frame.size.width + frame.origin.x) < (insetOval.size.width + insetOval.origin.x)),((frame.size.height + frame.origin.y) < (insetOval.size.height + insetOval.origin.y))]];
                return;
            } else if (ret == 0) {
                isCheckFaceCount = true;
                [self reco_msg_Liveness:feedBackframeMessage1];
                return;
            } else if (ret == -1) {
                isCheckFaceCount = true;
                [self reco_msg_Liveness:feedBackMultipleFaceMessage1];
                return;
            } else if (ret == -5) {
    //            head Message
                [self reco_msg_Liveness:feedBackFaceSteadyMessage1];
                isCheckFaceCount = true;
                return;
             } else if (ret == -8) {
                [self reco_msg_Liveness:feedBackLowLightMessage1];
                isCheckFaceCount = true;
                return;
            } else if (ret == -9) {
                [self reco_msg_Liveness:feedBackBlurFaceMessage1];
                isCheckFaceCount = true;
                return;
            } else if (ret == -10) {
                [self reco_msg_Liveness:feedBackGlareFaceMessage1];
                isCheckFaceCount = true;
                return;
            } else if (ret == 1) {

                if(ischekLivenss) {

                    CGFloat wX = pint[2] * 0.18;
                    CGFloat wY = pint[3] * 0.18;
                    CGFloat left = pint[0] - wX;
                    CGFloat top = pint[1] - wY;
                    CGFloat width = pint[2] + (wX * 2);
                    CGFloat height = pint[3] + (wY * 2);
                    CGRect extendOval1 = CGRectMake(left, top, width, height);

                   UIImage *img11 = [self cropToBounds:image11 :image11.size.width :image11.size.height];
                    UIImage *CropImage = [self cropImage:image11 cropToRect:extendOval1];

                    dispatch_async(dispatch_get_main_queue(), ^{
                    [self.delegate livenessData:img11 andshowImage:CropImage];
                    });
                }

            } else {
                isCheckFaceCount = true;
                return;
            }



    }
}

-(UIImage *)cropToBounds:(UIImage *)image :(CGFloat)width :(CGFloat)height{

    UIImage *contextImage = [[UIImage alloc] initWithCGImage:image.CGImage];
    CGSize contextSize = contextImage.size;
    CGFloat posX = 0.0;
    CGFloat posY = 0.0;
    CGFloat cgwidth = CGFloat(width);
    CGFloat cgheight = CGFloat(height);

    if (contextSize.width > contextSize.height) {
        posX = ((contextSize.width - contextSize.height) / 2);
        posY = 0;
        cgwidth = contextSize.height;
        cgheight = contextSize.height;
    } else {
        posX = 0;
        posY = ((contextSize.height - contextSize.width) / 2);
        cgwidth = contextSize.width;
        cgheight = contextSize.width;
//        cgwidth = 550;
//        cgheight = 550;
    }

    CGRect rect = CGRectMake(posX, posY, cgwidth, cgheight);
    CGImageRef imageRef = CGImageCreateWithImageInRect([contextImage CGImage], rect);
    UIImage *image1 = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
    return image1;
}

-(UIImage *)cropImage:(UIImage *)image cropToRect:(CGRect)rect{
    UIImage *contextImage = [[UIImage alloc] initWithCGImage:image.CGImage];


//    CGFloat width11 = (image.size.width / 1.7);
//    CGFloat height11 = (width11 * 1.4);
//    CGFloat newX = (image.size.width / 2) - (width11 / 2);
//    CGFloat newY = (image.size.height / 2) - (height11 / 2);
//
//    CGRect rect = CGRectMake(newX, newY, width11, height11);
    CGImageRef imageRef = CGImageCreateWithImageInRect([contextImage CGImage], rect);
    UIImage *image1 = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
    return image1;
}
-(void) reco_msg_Liveness:(NSString *)msg {
    livenessStatus = msg;//[NSString stringWithUTF8String:msg.c_str()];

//    doucumentMsg.text = [NSString stringWithUTF8String:msg.c_str()];


}
-(void) blink:(int)sec
{
    if(isShowlivenessMSG) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(sec * NSEC_PER_MSEC)), dispatch_get_main_queue(), ^{
            if(![feedbackLivenessMSG.text isEqual: @""]) {
                feedbackLivenessMSG.text = @"";
                [self blink:600];
            } else {
                feedbackLivenessMSG.text = livenessStatus;
                [self blink:1000];
            }

        });
    }
}

-(id)initWithDelegate:(UIViewController<VideoCameraWrapperDelegate>*)delegate andImageView:(UIImageView *)iv andFacePath:(NSString*)FacePath {
    
    imageView = iv;
    facePath = FacePath;
    
    facePathData = [facePath UTF8String];
    
    lines = @"";
    wholeresponce = jsonData();
    
    isFront = true;
    isCrrountCard = false;
    changeCard = "";
    cardPosition = -1;
    isMRZ = true;
    isFirstMRZ = true;
    isCheckMSG = true;
    isFirstMSG = true;
    isHologram = true;
    ischeckMation = true;
    inProcessing = false;
    ischekLivenss = false;
    
    
    PrimaryData primaryData = setTemplateFirst(firstTemp, wholeresponce, changeCard, cardPosition);
    changeCard = primaryData.cardSide;
    
    grayscaleMat = firstTemp;
    firstTemp.release();
    
    if (self = [super init]) {
        self.delegate = delegate;
        
        //                videoCamera = [[CvVideoCamera alloc] init];
        videoCamera = [[CvVideoCamera alloc] initWithParentView:imageView];
        videoCamera.delegate = self;
        videoCamera.defaultAVCaptureDevicePosition = AVCaptureDevicePositionBack;
        videoCamera.defaultAVCaptureSessionPreset = AVCaptureSessionPreset1280x720;
        videoCamera.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationPortrait;
        videoCamera.defaultFPS = 30;
        videoCamera.grayscaleMode = NO;
        videoCamera.rotateVideo = NO;

        docrecog_scan_RecogEngine_setBlurPercentage(55);
        docrecog_scan_RecogEngine_setFaceBlurPercentage(65);
        docrecog_scan_RecogEngine_setGlarePercentage(8, 99);
        docrecog_scan_RecogEngine_setHologramDetection(1);
        docrecog_scan_RecogEngine_setLowLightTolerance(40);
        docrecog_scan_RecogEngine_setMotionThreshold(15);

        

        NSString* path = [[NSBundle mainBundle] pathForResource:@"key" ofType:@"license"];
               NSLog(@"%@", path);
               if ([path isEqualToString:@""] || path == nil)
               {
                   [self.delegate recognizeFailed:@"key not found"];
                   return self;
               }

//               if(loadDiction((char*)[path UTF8String]) == 0)
//               {
//                   NSLog(@"Load Dic Failed");
//               }

//         if(loadDiction() == 0)
//         {
//             NSLog(@"Load Dic Failed");
//         }
        ret = docrecog_scan_RecogEngine_loadDictionary(path);
        if(ret < 0)
        {
            NSLog(@"Load Dic Failed");
//            ischeckLicense = false;
        }
    }
    
//    PrimaryData primaryData = setTemplateFirst(firstTemp, wholeresponce, changeCard, cardPosition);
//       changeCard = primaryData.cardSide;
//
//       grayscaleMat = firstTemp;
//       firstTemp.release();
    
    return self;
}

/*
 Call to Opencv framework method
 Parameters to Pass: scanning image CV::Mat metrix
 This method will return UIImage
 */

UIImage* uiimageFromCVMat(cv::Mat &cvMat)
{
    @autoreleasepool {
        if (cvMat.empty()) {
            return nil;
        }
        
        CGColorSpaceRef colorSpace;
        
        if (cvMat.channels() == 1) {
            colorSpace = CGColorSpaceCreateDeviceGray();
        } else if (cvMat.channels() == 3) {
            colorSpace = CGColorSpaceCreateDeviceRGB();
        } else {
            colorSpace = CGColorSpaceCreateDeviceRGB();
        }
        
        // Convert to RGBA if needed
        cv::Mat mat1 = cvMat.clone();
        if (cvMat.channels() == 4) {
            cv::cvtColor(mat1, mat1, cv::COLOR_BGRA2RGBA);
        }
        
        NSData *data = [NSData dataWithBytes:mat1.data length:mat1.elemSize() * mat1.total()];
        CGDataProviderRef provider = CGDataProviderCreateWithCFData((__bridge CFDataRef)data);
        
        CGImageRef imageRef = CGImageCreate(mat1.cols,  // Width
                                            mat1.rows,  // Height
                                            8,  // Bits per component
                                            8 * mat1.elemSize(),  // Bits per pixel
                                            mat1.step[0],  // Bytes per row
                                            colorSpace,  // Colorspace
                                            kCGImageAlphaPremultipliedLast | kCGBitmapByteOrderDefault,  // Bitmap info flags
                                            provider,  // CGDataProviderRef
                                            NULL,  // Decode
                                            false,  // Should interpolate
                                            kCGRenderingIntentDefault);  // Intent
        
        UIImage *image = [[UIImage alloc] initWithCGImage:imageRef];
        
        // Release resources
        CGImageRelease(imageRef);
        CGDataProviderRelease(provider);
        CGColorSpaceRelease(colorSpace);
        
        return image;
    }
}

/*
 Call to OpenCV framework method
 Param: scanning image
 Return:Mat metrix
 */

cv::Mat cvMatFromUIImage(UIImage* image)
{
    if (image == nil) {
        return cv::Mat::zeros(10, 10, CV_8UC4);
    }
    CGColorSpaceRef colorSpace = CGImageGetColorSpace(image.CGImage);
    CGFloat cols = image.size.width;
    CGFloat rows = image.size.height;
    
    cv::Mat cvMat(rows, cols, CV_8UC4); // 8 bits per component, 4 channels (color channels + alpha)
    
    CGContextRef contextRef = CGBitmapContextCreate(cvMat.data,                 // Pointer to  data
                                                    cols,                       // Width of bitmap
                                                    rows,                       // Height of bitmap
                                                    8,                          // Bits per component
                                                    cvMat.step[0],              // Bytes per row
                                                    colorSpace,                 // Colorspace
                                                    kCGImageAlphaNoneSkipLast |
                                                    kCGBitmapByteOrderDefault); // Bitmap info flags
    
    CGContextDrawImage(contextRef, CGRectMake(0, 0, cols, rows), image.CGImage);
    CGContextRelease(contextRef);
    cv::cvtColor(cvMat, cvMat, CV_RGBA2BGRA);
    return cvMat;
}

-(void)startCamera
{
    [videoCamera start];
    _isCapturing = YES;
    
    if(!ischekLivenss) {
        if (threadrunning == NO) {
            thread = [[NSThread alloc] initWithTarget:self selector:@selector(Recog_Thread) object:nil];
            [thread start];
        }
    } else {
        isShowlivenessMSG = true;
        inProcessingLiveness = true;
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
            inProcessingLiveness = false;
        });
    }
}

-(void)stopCamera
{
    cropImage = nil;
    finaldata.clear();
    [videoCamera stop];
    _isCapturing = NO;
    _matOrg.release();
    gimg.release();
    grayscaleMat.release();
    lines = @"";
    imageView = nil;
    [thread cancel];
      _livOrg.release();
    threadrunning = NO;
}

/*
 Delegate method for processing image frames
 Param: scanning cv::Mat metrix
 */

- (void)processImage:(cv::Mat&)image //This function is called per every frame
{

    if (ischekLivenss) {
        if (inProcessing) {
            return;
        }
        inProcessing = true;
    }
    [lock1 lock];
    _matOrg.release();
     _livOrg.release();
     if (ischekLivenss) {
         _livOrg = image.clone();
     }
    if (ischeckMation){
        ischeckMation = false;
            int doCheckData1 = doCheckData(image, image.cols,  image.rows);
        
            if (doCheckData1 == 0){
                
                    dispatch_async(dispatch_get_main_queue(), ^{
                        //Update UI
                        [self.delegate onMessage: @"0"];
//                        doucumentMsg.text = @"Keep Document Steady";
                        
                    });

                ischeckMation = true;
                _isMotion = NO;
            }else{

                _isMotion = YES;
            }
    }
    //crop
    UIImage *img = uiimageFromCVMat(image);
    CGPoint point = CGPointMake((([[UIScreen mainScreen] bounds].size.width / 2 ) - (viewScanningLayerWidth / 2 )), (([[UIScreen mainScreen] bounds].size.height / 2 ) - (viewScanningLayerHeight / 2 )));
    
    CGFloat hite =  0.0;
    CGFloat width = 0.0;
    width = img.size.width * scanningImgeWidthMultipler;
    hite = img.size.height * scanningImgeHeightMultipler;
    double fullWidth = img.size.width;
    double withImg = fullWidth * scanningImgeWidthMultipler;
    
    double originX = fullWidth - withImg;
    
    // Setup a rectangle to define your region of interest
    cv::Rect myROI(originX, point.y, width, hite);
    // Crop the full image to that image contained by the rectangle myROI
    cv::Mat croppedImage = image(myROI);
    
    _matOrg = croppedImage.clone();
    [lock1 unlock];
    
    if (self->_isCapturing) {
        [self performSelectorOnMainThread:@selector(ShowImg) withObject:nil waitUntilDone:NO];
    }
}

/*
 This method calls frame for scanning MRZ documents.
 Device orientation according and sets scanning view frame
 */

-(void)ChangedOrintation:(CGFloat)width height:(CGFloat)height {
    
    navigationHeight = 140.0;
    scanningOriginX = 0;
    scanningImgeHeightMultipler = 0.35;
    scanningImgeWidthMultipler = 0.95;
    
    viewScanningLayerWidth = width;
    viewScanningLayerHeight = height;
}

- (void) ShowImg
{
    [lock1 lock];
    if (_matOrg.empty()) {
        [lock1 unlock];
        return;
    }
    
    cv::Mat matShow;
    _matOrg.copyTo(matShow);
    [lock1 unlock];
    
    if (ischekLivenss){
        [self.delegate processedImage: uiimageFromCVMat(_livOrg)];
    }else{
        [self.delegate processedImage: uiimageFromCVMat(matShow)];
    }

    if (ischekLivenss){
        inProcessing = false;
        if (isCheckFaceCount){
            if(inProcessingLiveness) {
                return;
            }
            isCheckFaceCount = false;
            UIImage* img = uiimageFromCVMat(_livOrg);
//            dispatch_async(dispatch_get_main_queue(), ^{
                [self drawFeaturesForFace:img];
//            });
        }
    }
    matShow.release();
}

- (void) Recog_Thread
{
    threadrunning = YES;
    
    while (true) {
        [NSThread sleepForTimeInterval:0.05];
        
        if (ret < 0) {
                   switch (ret) {
                       case -1:
                            [_delegate recognizeFailed:@"Invalid License"];
                           break;
                       case -2:
                           [_delegate recognizeFailed:@"Invalid BundleID"];
                           break;
                       case -3:
                           [_delegate recognizeFailed:@"Invalid Platform"];
                           break;
                       case -4:
                           [_delegate recognizeFailed:@"License is Expired"];
                           break;
                       default:
                           break;
                   }
                   break;
               }

        if (threadrunning == NO) {
            break;
        }
        
        if (_isCapturing == NO) {
            continue;
        }
        if (_isMotion == NO) {
            continue;
        }
        [lock1 lock];
        if (_matOrg.empty()) {
            [lock1 unlock];
            continue;
        }
        
        if ((_matOrg.cols != 720) && (_matOrg.cols != 0)){
            _matOrg.copyTo(img);
            [lock1 unlock];
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            if (isFirstMSG){
                isFirstMSG = false;
                [self.delegate onMessage: @"1"];
                [self refreshPreview];
            }
            
        });
        
        [self Recog_Thread119:img];
        
        img.release();
    }
    threadrunning = NO;
}

-(void) Recog_MRZ:(cv::Mat&)mrzImg{
//    CFTimeInterval tf = CACurrentMediaTime();
//    cv::Mat splits[4];
//    cv::split(mrzImg, splits);
//
//    int w = mrzImg.cols;
//    int h = mrzImg.rows;
    int w = mrzImg.cols;
        int h = mrzImg.rows;
        int sw = 1200;
        float scale = (float)w/(float)h;
        //            float fscalex = (float)sw / (float)_matOrg.cols;// src_pix->w);
        //            int sh = ((int)(_matOrg.rows*fscalex * 8 + 31) / 32) * 4;
        int sh = sw/(float)scale;
        cv::Mat splits[4];
        cv::Mat cpmat, frameMat;
        mrzImg.copyTo(frameMat);
        cv::resize(frameMat, frameMat, cv::Size(sw,sh));
        cv::split(frameMat, splits);
        frameMat.release();
        mrzImg.copyTo(cpmat);


    
    char chsurname[100],chgivenname[100];
    char chlines[100];
    char chtype[100];
    char chcountry[100];
    char chpassportnumber[100],chpassportchecksum[100];
    char chnationality[100];
    char chbirth[100];char chbirthchecksum[100];
    char chsex[100];
    char chexpirationdate[100],chexpirationchecksum[100];
    char chpersonalnumber[100],chpersonalnumberchecksum[100];
    char chsecondrowchecksum[100];
    char chplaceofbirth[100];
    char chplaceofissue[100];
    
    unsigned char *photoChannels[3];
    photoChannels[0] = new unsigned char[400*400];
    photoChannels[1] = new unsigned char[400*400];
    photoChannels[2] = new unsigned char[400*400];
    int phoW = 0, phoH = 0;
    
    bool bPickPhoto = YES;
    NSString* path = [[NSBundle mainBundle] pathForResource:@"key" ofType:@"license"];
    NSLog(@"%@", path);
    if ([path isEqualToString:@""] || path == nil)
    {
        [self.delegate recognizeFailed:@"key not found"];
        //                    break;
    }
    
    // check the rectype
    /*
     SDK Method to do MRZ Scan
     */
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.delegate onMessage: @"3"];
    });
    
    retval = doRecogGrayImg_Passport(splits[2].data, splits[1].data, splits[0].data, sw, sh, chlines, success, chtype, chcountry, chsurname, chgivenname, chpassportnumber, chpassportchecksum, chnationality, chbirth, chbirthchecksum,chsex, chexpirationdate, chexpirationchecksum, chpersonalnumber, chpersonalnumberchecksum, chsecondrowchecksum,chplaceofbirth,chplaceofissue, photoChannels[0], photoChannels[1], photoChannels[2],&phoW, &phoH, bPickPhoto,(char*)[path UTF8String]);
    
//    tf = CACurrentMediaTime() - tf;
//    NSLog(@"recogend %f",tf);
    if(success == true)
    {
        isCheckMSG = false;
        //            if (isMRZ){
        lines = [NSString stringWithUTF8String:chlines];
        passportType = [NSString stringWithUTF8String:chtype];
        country = [NSString stringWithUTF8String:chcountry];
        surName = [NSString stringWithUTF8String:chsurname];
        givenNames = [NSString stringWithUTF8String:chgivenname];
        passportNumber = [NSString stringWithUTF8String:chpassportnumber];
        passportNumberChecksum = [NSString stringWithUTF8String:chpassportchecksum];
        nationality = [NSString stringWithUTF8String:chnationality];
        birth = [NSString stringWithUTF8String:chbirth];
        BirthChecksum = [NSString stringWithUTF8String:chbirthchecksum];
        sex = [NSString stringWithUTF8String:chsex];
        expirationDate = [NSString stringWithUTF8String:chexpirationdate];
        expirationDateChecksum = [NSString stringWithUTF8String:chexpirationchecksum];
        personalNumber = [NSString stringWithUTF8String:chpersonalnumber];
        personalNumberChecksum = [NSString stringWithUTF8String:chpersonalnumberchecksum];
        secondRowChecksum = [NSString stringWithUTF8String:chsecondrowchecksum];
        placeOfBirth = [NSString stringWithUTF8String:chplaceofbirth];
        placeOfIssue = [NSString stringWithUTF8String:chplaceofissue];
        
        [self performSelectorOnMainThread:@selector(Recog_Successed) withObject:nil waitUntilDone:YES];
    }else{
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onMessage: @""];
            [self refreshPreview];
        });
    }
    
    //remove splites
    splits[0].release();
    splits[1].release();
    splits[2].release();
    
    delete[] photoChannels[0];
    delete[] photoChannels[1];
    delete[] photoChannels[2];
    
}

-(void) Recog_Thread119:(cv::Mat&)matImg{
    if ((matImg.cols !=  720) && (matImg.cols != 0)){
        cropImage1 = uiimageFromCVMat(matImg);
        ImageOpenCv imageOpenCv = checkCardInFrameOrNot(grayscaleMat, matImg, gimg, facePathData, isHologram);
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (!isCheckMSG){
                [self.delegate onMessage: @"3"];
            }else{
                
                if (imageOpenCv.message != ""){
                   [self reco_msg:imageOpenCv.message];
                }
                //  [self reco_msg:imageOpenCv.message];
                
                if (imageOpenCv.message ==  "4"){
                    
                    AVCaptureDevice *acd=[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
                    
                    if ([acd isFocusModeSupported:AVCaptureFocusModeAutoFocus] && [acd isFocusPointOfInterestSupported])
                    {
                        if ([acd lockForConfiguration:nil])
                        {
                            [acd setFocusMode:AVCaptureFocusModeAutoFocus];
                            
                            [acd unlockForConfiguration];
                        }
                    }
                    
                }
                
            }
            
        });
        
        if (imageOpenCv.isSucess){
                
            ischeckMation = true;
            if (changeCard == "Backside"){
                [self Recog_MRZ:gimg];
            }
            
            finaldata = imageOpenCv.mapData;
            
            if (finaldata.size() == 0){
                return;
            }
            [self performSelectorOnMainThread:@selector(recog_Successed) withObject:nil waitUntilDone:YES];
        }else{
                
            ischeckMation = true;
            if(!isCheckMSG){
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.delegate onMessage: @""];
                    [self refreshPreview];
                });
            }
            
            matImg.release();
            gimg.release();
        }
    }
}

- (void) recog_Successed
{
    cropImage = uiimageFromCVMat(gimg);
    if (changeCard == "FrontSide"){
        lines = @"";
        
        isMRZ = true;
        grayscaleMat.release();
        string result[finaldata.size()][2];
        
        NSMutableDictionary *inner = [[NSMutableDictionary alloc] init];
        
        for (auto itr = finaldata.begin(); itr != finaldata.end(); ++itr){
            int pos = itr->first;
            // Get whole Data of element
            auto x = itr->second;
            //get element name
            std::string key = x.key_;
            //get rect of element
            std::string data = x.data_;
            result[pos][0] = key;
            result[pos][1] = data;
            
            NSString *finalKey = [NSString stringWithUTF8String:key.c_str()];
            NSString *finalData = [NSString stringWithUTF8String:data.c_str()];
            
            [inner setObject:[NSString stringWithFormat:@"%@", finalData] forKey:finalKey];
        }
        
        [_delegate matchedItem:cropImage dict:inner];
        //        isFirstMRZ = false;
        isHologram = false;
        cv::Mat rimg;
        
        PrimaryData primaryData =  setTemplateFirst(rimg, wholeresponce, "Backside", -1);
        
        changeCard = primaryData.cardSide;
        
        grayscaleMat = rimg;
        
        rimg.release();
        
        [inner removeAllObjects];
        
    }else{
        if(![lines isEqualToString:@""]){
            threadrunning = NO;
            string result[finaldata.size()][2];
            
            NSMutableDictionary *inner = [[NSMutableDictionary alloc] init];
            
            for (auto itr = finaldata.begin(); itr != finaldata.end(); ++itr){
                int pos = itr->first;
                // Get whole Data of element
                auto x = itr->second;
                //get element name
                std::string key = x.key_;
                //get rect of element
                std::string data = x.data_;
                result[pos][0] = key;
                result[pos][1] = data;
                
                NSString *finalKey = [NSString stringWithUTF8String:key.c_str()];
                NSString *finalData = [NSString stringWithUTF8String:data.c_str()];
                [inner setObject:[NSString stringWithFormat:@"%@", finalData] forKey:finalKey];
            }
            
            [_delegate matchedItem:cropImage dict:inner];
            
            success = false;
            [inner removeAllObjects];
        }
    }
}

/*
 * Method called after MRZ scanned successfull
 */
- (void) Recog_Successed
{
    NSMutableDictionary *shareScanningListing = [[NSMutableDictionary alloc]init];
    [shareScanningListing setValue: lines forKey: @"lines"];
    [shareScanningListing setValue: passportType forKey: @"passportType"];
    [shareScanningListing setValue: country forKey: @"country"];
    [shareScanningListing setValue: surName forKey: @"surName"];
    [shareScanningListing setValue: givenNames forKey: @"givenNames"];
    [shareScanningListing setValue: passportNumber forKey: @"passportNumber"];
    [shareScanningListing setValue: passportNumberChecksum forKey: @"passportNumberChecksum"];
    [shareScanningListing setValue: nationality forKey: @"nationality"];
    [shareScanningListing setValue: birth forKey: @"birth"];
    [shareScanningListing setValue: BirthChecksum forKey: @"BirthChecksum"];
    [shareScanningListing setValue: sex forKey: @"sex"];
    [shareScanningListing setValue: expirationDate forKey: @"expirationDate"];
    [shareScanningListing setValue: expirationDateChecksum forKey: @"expirationDateChecksum"];
    [shareScanningListing setValue: personalNumber forKey: @"personalNumber"];
    [shareScanningListing setValue: personalNumberChecksum forKey: @"personalNumberChecksum"];
    [shareScanningListing setValue: secondRowChecksum forKey: @"secondRowChecksum"];
    [shareScanningListing setValue: placeOfBirth forKey: @"placeOfBirth"];
    [shareScanningListing setValue: [NSString stringWithFormat:@"%d",retval] forKey: @"retval"];
    [shareScanningListing setValue: placeOfIssue forKey: @"placeOfIssue"];
    
    if (![lines isEqualToString:@""]){
        [[NSUserDefaults standardUserDefaults] setObject:shareScanningListing forKey:@"ScanningDataMRZ"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        isMRZ = false;
        isFirstMRZ = true;
    }
}

-(void) reco_msg:(string)imgMsg
{
    [self.delegate onMessage: [NSString stringWithUTF8String:imgMsg.c_str()]];
}

-(void) refreshPreview
{
    inProcessing = false;
    isCheckFaceCount = true;
}

@end
