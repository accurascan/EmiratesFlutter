//
//  EngineWrapper.h
//  FaceMatch


#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class NSFaceRegion;

@interface EngineWrapper : NSObject

+(void) FaceEngineInit;
+(BOOL) IsEngineInit;
+(int) GetEngineInitValue;
+(void) FaceEngineClose;
+(NSFaceRegion*) DetectSourceFaces:(UIImage*) image;
+(NSFaceRegion*) DetectTargetFaces:(UIImage*) image feature1:(NSData*) feature1;
+(double) Identify:(NSData*)pbuff1 featurebuff2:(NSData*)pbuff2;

@end
