//
//  NSFaceRegion.h


#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@interface NSFaceRegion : NSObject
{
}

@property (assign, atomic) int face;
@property (assign, atomic) CGRect  bound;
@property (assign, atomic) double confidence;
@property (nonatomic, strong) NSData *feature;
@property (assign, atomic) UIImage* image;

@end
