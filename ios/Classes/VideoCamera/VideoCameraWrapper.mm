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

@interface VideoCameraWrapper() <CvVideoCameraDelegate>
@end


@implementation VideoCameraWrapper
{
    CvVideoCamera *videoCamera;
    cv::Mat _matOrg;
    
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
    //Check input cv::mat empty or not
    @autoreleasepool {
        if (cvMat.empty()) {
            return nil;
        }
        
        CGColorSpaceRef colorSpace;
        
        if (cvMat.elemSize() == 1) {
            colorSpace = CGColorSpaceCreateDeviceGray();
        } else {
            colorSpace = CGColorSpaceCreateDeviceRGB();
        }
        cv::Mat mat1 = cvMat.clone();
        if (cvMat.elemSize() == 4) {
            cv::cvtColor(mat1, mat1, CV_BGRA2RGBA);
        }
        NSData *data = [NSData dataWithBytes:mat1.data length:mat1.elemSize() * mat1.total()];
        
        CGDataProviderRef provider = CGDataProviderCreateWithCFData((__bridge CFDataRef)data);
        
        CGImageRef imageRef = CGImageCreate(mat1.cols, // Width
                                            mat1.rows, // Height
                                            8, // Bits per component
                                            8 * mat1.elemSize(), // Bits per pixel
                                            mat1.step[0], // Bytes per row
                                            colorSpace, // Colorspace
                                            kCGImageAlphaNone | kCGBitmapByteOrderDefault, // Bitmap info flags
                                            provider, // CGDataProviderRef
                                            NULL, // Decode
                                            false, // Should interpolate
                                            kCGRenderingIntentDefault); // Intent
        
        UIImage *image = [[UIImage alloc] initWithCGImage:imageRef];
        CGImageRelease(imageRef);
        CGDataProviderRelease(provider);
        CGColorSpaceRelease(colorSpace);
        mat1.release();
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
    
    if (threadrunning == NO) {
        thread = [[NSThread alloc] initWithTarget:self selector:@selector(Recog_Thread) object:nil];
        [thread start];
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
    
    threadrunning = NO;
}

/*
 Delegate method for processing image frames
 Param: scanning cv::Mat metrix
 */

- (void)processImage:(cv::Mat&)image //This function is called per every frame
{
    
    [lock1 lock];
    _matOrg.release();
     
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
    
    [self.delegate processedImage: uiimageFromCVMat(matShow)];
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
            }
            
        });
        
        [self Recog_Thread119:img];
        
        img.release();
    }
    threadrunning = NO;
}

-(void) Recog_MRZ:(cv::Mat&)mrzImg{
    CFTimeInterval tf = CACurrentMediaTime();
    cv::Mat splits[4];
    cv::split(mrzImg, splits);
    
    int w = mrzImg.cols;
    int h = mrzImg.rows;
    
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
    
    retval = doRecogGrayImg_Passport(splits[2].data, splits[1].data, splits[0].data, w, h, chlines, success, chtype, chcountry, chsurname, chgivenname, chpassportnumber, chpassportchecksum, chnationality, chbirth, chbirthchecksum,chsex, chexpirationdate, chexpirationchecksum, chpersonalnumber, chpersonalnumberchecksum, chsecondrowchecksum,chplaceofbirth,chplaceofissue, photoChannels[0], photoChannels[1], photoChannels[2],&phoW, &phoH, bPickPhoto,(char*)[path UTF8String]);
    
    tf = CACurrentMediaTime() - tf;
    NSLog(@"recogend %f",tf);
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

@end
