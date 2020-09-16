//
//  EngineWrapper.m
//  FaceMatch
                                                                                                        

#include <opencv2/imgproc/imgproc.hpp>
#import "EngineWrapper.h"
#import "ImageHelper.h"
#import "NSFaceRegion.h"
//
#include "types.h"
#include "faceengine.h"
#include "zinterface.mm"

int g_nEngineInit = -100;

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
    float* feature1 = (float*)[pbuff1 bytes];
    int   len1 = [pbuff1 length];
    float* feature2 = (float*)[pbuff2 bytes];
    int   len2 = [pbuff2 length];
    
    float score = 0.0;
    if (len1 == 0 || len2 == 0 || feature1 == nil || feature2 == nil)
        return 0.0f;
    
        SResult ret = Identify(len1, feature1, len2, feature2, &score);
        if (ret != wOK)
        {
            return 0.0;
        }
    return score;
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
+(NSFaceRegion*) DetectTargetFaces:(UIImage*) image feature1:(NSData*) feature1
{
    
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
