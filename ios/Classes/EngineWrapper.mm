//
//  EngineWrapper.m
//  FaceMatch
                                                                                                        

#include <opencv2/imgproc/imgproc.hpp>
#import "EngineWrapper.h"
#import "ImageHelper.h"
#import "NSFaceRegion.h"
//
#import <UIKit/UIKit.h>
#include "types.h"
#include "faceengine.h"
#include "zinterface.mm"

int g_nEngineInit = -100;
NSFaceRegion* invertedImg;

@implementation EngineWrapper

+(void) FaceEngineInit
{
    NSString *dataFilePath1 = [[NSBundle mainBundle] pathForResource:@"model1" ofType:@"dat"];
    NSString *dataFilePath2 = [[NSBundle mainBundle] pathForResource:@"model2" ofType:@"dat"];
    NSString *licensePath = [[NSBundle mainBundle] pathForResource:@"accuraface" ofType:@"license"];
    
    NSLog(@"dataFilePath1 %@",dataFilePath1);
     NSLog(@"dataFilePath2 %@",dataFilePath2);
     NSLog(@"licensePath %@",licensePath);
    if ([licensePath isEqualToString:@""] || licensePath == nil)
    {
        g_nEngineInit = -20;
        return; //license file not exist
    }
    
    SResult ret = InitEngine([dataFilePath1 UTF8String], [dataFilePath2 UTF8String], [licensePath UTF8String]);
    
    g_nEngineInit = ret;
}

+(BOOL) IsEngineInit
{
    return (g_nEngineInit == wOK);
}

+(int) GetEngineInitValue
{
    return g_nEngineInit;
}

/**
 * This method use for identify face match score.
 * Parameters to Pass: front image data and back image data.
 *
 * This method will return int score.
 */
+(double) Identify:(NSData*)pbuff1 featurebuff2:(NSData*)pbuff2
{
    NSData* pbuffIn = invertedImg.feature;
    
    float* feature1 = (float*)[pbuff1 bytes];
    int   len1 = [pbuff1 length];
    float* feature2 = (float*)[pbuff2 bytes];
    int   len2 = [pbuff2 length];
    float* feature3 = (float*)[pbuffIn bytes];
    int   len3 = [pbuffIn length];
    
//    float score = 0.0;
    float score1 = 0.0;
    float score2 = 0.0;
    
    if (len1 == 0 || len2 == 0 || len3 == 0 || feature1 == nil || feature2 == nil || feature3 == nil)
        return 0.0f;
    
        SResult ret1 = Identify(len1, feature1, len2, feature2, &score1);
        if (ret1 != wOK)
        {
            return 0.0;
        }
        SResult ret2 = Identify(len1, feature1, len3, feature3, &score2);
        if (ret2 != wOK)
        {
            return 0.0;
        }
    if(score1 < 0.60 || score2 < 0.60)
    {
        return MIN(score1, score2);
    }else{
        return MAX(score1, score2);
    }
//    return score;
}

/**
 * This method use for identify face in front image.
 * Parameters to Pass: front image data
 *
 * This method will return image.
 */
+(NSFaceRegion*) DetectSourceFaces:(UIImage*) image
{
    
    if ([self IsEngineInit] == NO)
        return nil;
    
    NSFaceRegion* region = [[NSFaceRegion alloc] init];
    region.image = nil;
    unsigned char* inbits = [ImageHelper bitmapFromImage:image];
    if (inbits == NULL)
    {
        NSLog(@"Image buffer is Null");
        return nil;
    }
    
    int imgSize = ([image size].width * 32 + 31) / 32 * 4 * [image size].height ;
    int imgWidth = [image size].width;
    int imgHeight = [image size].height;
    
    int nFaceCount = 0;
    SFaceExt pFaces;
    SResult ret = DetectSourceFace(inbits, (DWORD)imgSize, (DWORD)imgWidth, (DWORD)imgHeight, (int *)&nFaceCount, &pFaces);
    
    if (ret == wOK) {
        if (nFaceCount > 0) {
            SWRect rect = pFaces.Rectangle;
            CGFloat fx = (CGFloat)rect.X;
            CGFloat fy = (CGFloat)rect.Y;

            CGFloat fw = (CGFloat)rect.Width;
            CGFloat fh = (CGFloat)rect.Height;
            
            region.bound = CGRectMake(fx, fy, fw, fh);

            region.confidence = pFaces.Confidence;
            region.face = 1;
            
            region.feature = [NSData dataWithBytes:pFaces.featureData length:pFaces.nFeatureSize*sizeof(float)];
                
        }
    }
    region.image = image;
    free(inbits);
    return region;
}

/**
 * This method use for identify face in back image which found in front image.
 * Parameters to Pass: back image and front image data
 *
 * This method will return image.
 */

UIImage* horizontallyInvertedImage(UIImage *image) {
    UIGraphicsBeginImageContextWithOptions(image.size, NO, image.scale);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Move the origin to the middle of the image so we can flip it
    CGContextTranslateCTM(context, image.size.width / 2, image.size.height / 2);
    CGContextScaleCTM(context, -1.0, 1.0);
    CGContextTranslateCTM(context, -image.size.width / 2, -image.size.height / 2);
    
    // Draw the image into the context
    [image drawAtPoint:CGPointZero];
    
    // Get the new image
    UIImage *invertedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return invertedImage;
}

+(NSFaceRegion*) DetectTargetFaces:(UIImage*) image feature1:(NSData*) feature1
{
    UIImage* invImg = horizontallyInvertedImage(image);
    if ([self IsEngineInit] == NO)
        return nil;
    
    NSFaceRegion* region = [[NSFaceRegion alloc] init];
    region.image = nil;
    unsigned char* inbits = [ImageHelper bitmapFromImage:image];
    if (inbits == NULL)
    {
        NSLog(@"Image buf fer is Null");
        return nil;
    }
    
    int imgSize = ([image size].width * 32 + 31) / 32 * 4 * [image size].height ;
    int imgWidth = [image size].width;
    int imgHeight = [image size].height;
    float* pFeature1 = (float*)[feature1 bytes];
    
    int nFaceCount = 0;
    SFaceExt pFaces;
    SResult ret = DetectTargetFace(inbits, (DWORD)imgSize, (DWORD)imgWidth, (DWORD)imgHeight, (int *)&nFaceCount, &pFaces, pFeature1);
    
    if (ret == wOK) {
        if (nFaceCount > 0) {
            SWRect rect = pFaces.Rectangle;
            CGFloat fx = (CGFloat)rect.X;
            CGFloat fy = (CGFloat)rect.Y;
            
            CGFloat fw = (CGFloat)rect.Width;
            CGFloat fh = (CGFloat)rect.Height;
            
            region.bound = CGRectMake(fx, fy, fw, fh);
            region.confidence = pFaces.Confidence;
            region.face = 1;
            
            region.feature = [NSData dataWithBytes:pFaces.featureData length:pFaces.nFeatureSize*sizeof(float)];
            
        }
    }
    region.image = image;
    free(inbits);
    
    if ([self IsEngineInit] == NO)
        return nil;
    
    NSFaceRegion* regionN = [[NSFaceRegion alloc] init];
    regionN.image = nil;
    unsigned char* inbitsN = [ImageHelper bitmapFromImage:invImg];
    if (inbitsN == NULL)
    {
        NSLog(@"Image buf fer is Null");
        return nil;
    }
    
    int imgSizeN = ([invImg size].width * 32 + 31) / 32 * 4 * [invImg size].height ;
    int imgWidthN = [invImg size].width;
    int imgHeightN = [invImg size].height;
    float* pFeature1N = (float*)[feature1 bytes];
    
    int nFaceCountN = 0;
    SFaceExt pFacesN;
    SResult retN = DetectTargetFace(inbitsN, (DWORD)imgSizeN, (DWORD)imgWidthN, (DWORD)imgHeightN, (int *)&nFaceCountN, &pFacesN, pFeature1N);
    
    if (retN == wOK) {
        if (nFaceCountN > 0) {
            SWRect rectN = pFacesN.Rectangle;
            CGFloat fxN = (CGFloat)rectN.X;
            CGFloat fyN = (CGFloat)rectN.Y;
            
            CGFloat fwN = (CGFloat)rectN.Width;
            CGFloat fhN = (CGFloat)rectN.Height;
            
            regionN.bound = CGRectMake(fxN, fyN, fwN, fhN);
            regionN.confidence = pFacesN.Confidence;
            regionN.face = 1;
            
            regionN.feature = [NSData dataWithBytes:pFacesN.featureData length:pFacesN.nFeatureSize*sizeof(float)];
            
        }
    }
    regionN.image = invImg;
    free(inbitsN);
    invertedImg = regionN;
    
    return region;
}

+(void) FaceEngineClose
{
    if ([self IsEngineInit] == NO)
        return;
    
    g_nEngineInit = -100;
    
    CloseEngine();
}


@end
