//
//  Accura.h
//  AccuraSDK
//
//  Created by kuldeep on 12/16/19.
//  Copyright © 2019 Elite Development LLC. All rights reserved.
//

#ifndef Accura_h
#define Accura_h
#include "opencv2/opencv.hpp"
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#import <vector>
#include <string>
#include <queue>
#include <regex>
#include <map>
#include "json.hpp"
#include "base64.h"

using namespace cv;
using namespace std;

typedef struct Recte {
    Recte(int i, int i1, int i2, int i3) {
        left = i;
        top = i1;
        right = i2;
        bottom = i3;
        
    }
    
    int left;
    int top;
    int right;
    int bottom;
    
    bool valueInRange(int value, int min, int max) { return (value >= min) && (value <= max); }
    
public:
    bool rectOverlap(Recte A, Recte B) {
        bool xOverlap = valueInRange(A.left, B.left, B.right) ||
        valueInRange(B.left, A.left, A.right);
        
        bool yOverlap = valueInRange(A.top, B.top, B.bottom) ||
        valueInRange(B.top, A.top, A.bottom);
        
        return xOverlap && yOverlap;
    }
    
    bool contains(Recte R1, Recte R2) {
        if ((R2.right) < (R1.right)
            && (R2.left) > (R1.left)
            && (R2.top) > (R1.top)
            && (R2.bottom) < (R1.bottom)
            ) {
            return true;
        } else {
            return false;
        }
    }
} RECTE, *PRECTE;


struct Data_t {
    std::string key_;
    std::string data_;
    
    Data_t(std::string key_v, std::string data_v) : key_(key_v), data_(data_v) {}
};

struct Rect_t {
    std::string key_;
    Recte data_;
    
    Rect_t(std::string key_v, Recte data_v) : key_(key_v), data_(data_v) {}
};

struct Required_t {
    std::string key_;
    int data_;
    
    
    Required_t(std::string key_v, int data_v) : key_(key_v), data_(data_v) {}
};


class MinMaxLocResult {
public: MinMaxLocResult(double mnV, double mxV, cv::Point mnI, cv::Point mxI){
    minval = mnV;
    maxVal = mxV;
    minLoc = mnI;
    maxLoc = mxI;
};
    
    bool isNull() {
        if (minval == NULL) {
            return true;
        }
        if (maxVal == NULL) {
            return true;
        }
        if ((minLoc.x == NULL) || (minLoc.y == NULL)) {
            return true;
        }
        if ((maxLoc.x == NULL) || (maxLoc.y == NULL)) {
            return true;
        }
        return false;
        
    }
    
    MinMaxLocResult() {
        minval = NULL;
        maxVal = NULL;
        minLoc.x = NULL;
        minLoc.y = NULL;
        maxLoc.x = NULL;
        maxLoc.y = NULL;
    }
    
    double maxVal;
    double minval;
    cv::Point minLoc, maxLoc;
    float ratio;
};

class ImageOpenCv {
    
public:
    ImageOpenCv(const string &message, bool isSucess, bool isChangeCard, int cardPos,
                int resultCode, float ratioOut, map<int, Data_t> mapData) : message(message),
    isSucess(isSucess),
    isChangeCard(
                 isChangeCard),
    cardPos(cardPos),
    resultCode(
               resultCode),
    ratioOut(ratioOut),
    mapData(mapData) {
    }
    
    string message = "";
    bool isSucess = false;
    bool isChangeCard = false;
    int cardPos = 0;
    int resultCode = 0;
    float ratioOut;
    map<int, Data_t> mapData;
};

class PrimaryData {
public:
    PrimaryData(const string &imageName, const string &cardSide, const bool &isFront,
                int cardPos, float refWidth, float refHeight, int imgHeight,
                vector<string> rect)
    : imageName(imageName), cardSide(cardSide), isFront(isFront), cardPos(cardPos),
    refWidth(refWidth), refHeight(refHeight), imgHeight(imgHeight), rect(rect) {}
    
    string imageName = "";
    string cardSide = "";
    bool isFront = true;
    int cardPos = 0;
    float refWidth = 0;
    float refHeight = 0;
    int imgHeight = 0;
    vector<string> rect;
};

//namespace
//{

bool doGrayScaleCheck(cv::Mat &mat);

bool doBlurCheck(cv::Mat &mat);

bool doGlareCheck(cv::Mat &mat);

MinMaxLocResult multiScaleTemplateMatch(cv::Mat &refMat, cv::Mat &src, float d);

MinMaxLocResult CropBitmap(Mat &tempmat, Mat &inputmat, Mat &resultMat);

int getCroppedCard(cv::Mat &mat, cv::Mat &resultMat, cv::Rect rect);

ImageOpenCv
checkCardInFrameOrNot(Mat &refMat, Mat &src, Mat &resultMat, string basicString, bool isHologram);

ImageOpenCv getObject(string basicString, bool sucess, bool card, int pos, float out, int i,
                      map<int, Data_t> map);


ImageOpenCv
checkCardInFrameOrNot(Mat &refMat, Mat &src, Mat &resultMat, string basicString, bool isHologram);

PrimaryData
setTemplateFirst(Mat &outMat, nlohmann::json wholeresponce1, string string1, int i1);

nlohmann::json jsonData();

 int closeOCR(int clear_ocr_data);

int doCheckData(cv::Mat rgbMat, int w, int h);

int docrecog_scan_RecogEngine_setBlurPercentage(int jint1);
int docrecog_scan_RecogEngine_setFaceBlurPercentage(int jint1);
int docrecog_scan_RecogEngine_setGlarePercentage(int jintMin, int jintMax);
int docrecog_scan_RecogEngine_setHologramDetection(bool jint1);
int docrecog_scan_RecogEngine_setLowLightTolerance(int tolerance);
int docrecog_scan_RecogEngine_setMotionThreshold(int motionTolerance);
void setPrintLogs(bool b, std::string documentDirectory);


#endif /* Accura_h */
