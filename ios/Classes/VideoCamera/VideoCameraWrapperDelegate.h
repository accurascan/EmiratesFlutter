//
//  VideoCameraWrapperDelegate.h
//  AccuraSDK
//
//  Created by Chang Alex on 1/26/20.
//  Copyright © 2020 Elite Development LLC. All rights reserved.
//


#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>


typedef NS_ENUM(NSUInteger, RecType) {
    REC_INIT = 1001,
    REC_BOTH,
    REC_FACE,
    REC_MRZ
};

@protocol VideoCameraWrapperDelegate <NSObject>
@optional
- (void)processedImage:(UIImage*)image;
- (void)recognizeFailed:(NSString*)message;
- (void)onMessage:(NSString*)message;
- (void)matchedItem:(UIImage*)image dict:(NSMutableDictionary*)setData;
-(void)livenessData:(UIImage*)livenessImage andshowImage:(UIImage*)showImage;
@end