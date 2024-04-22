//
//  DectectShape.h
//  DectectShape

#import <Foundation/Foundation.h>
#import <UIKit/UIImage.h>
#include "opencv2/core/core.hpp"
#include "types.h"


int loadDiction();

//int loadDiction(char* licenseFilePath);

//photoImgR,G,B = 200*200 should be allocated first.
int doRecogGrayImg_Passport(unsigned char* rImg,unsigned char* gImg,unsigned char* bImg, int w, int h,/*int left,int top,int width,int height,*/char* lines, bool& success, char* passportType, char* country,char* surName,char* givenNames,char* passportNumber,char* passportChecksum,char* nationality, char* birth,char* birthChecksum,char* sex,char* expirationDate,char* expirationChecksum,char* personalNumber,char* personalNumberChecksum,char* secondRowChecksum,char* placeOfBirth,char* placeOfIssue,unsigned char* photoImgR,unsigned char* photoImgG,unsigned char* photoImgB, int *phoW, int *phoH, bool bCropPhoto,char* licenseFilePath);

int doFaceDetect(unsigned char* rImg, unsigned char* gImg, unsigned char* bImg, int w, int h, unsigned char* photoImgR, unsigned char* photoImgG, unsigned char* photoImgB, int* phoW, int *phoH);

int docrecog_scan_RecogEngine_loadDictionary(NSString* licenseFilePath);

int accurascan_facedetection_facedetectionutils_FaceDetectionProcessor_detectFace(cv::Mat srcMat,int left,int top,int width,int height,int pintData[]);

int accurascan_facedetection_facedetectionutils_FaceDetectionProcessor_initEngine(std::string model1, int light_threshold,int blur_threshold, int g_min, int g_max);

int checklivenessValidation(cv::Mat mat,int faceblur,int glaremin,int glaremax,int light);