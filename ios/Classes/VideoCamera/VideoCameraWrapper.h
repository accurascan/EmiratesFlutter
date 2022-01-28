//
//  VideoCameraWrapper.h
//  AccuraSDK
//
//  Created by Chang Alex on 1/26/20.
//  Copyright © 2020 Elite Development LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "VideoCameraWrapperDelegate.h"

@interface VideoCameraWrapper : NSObject

{
    BOOL _isCapturing;
    NSThread *thread;
    BOOL _isMotion;
    
}

@property (nonatomic, strong) id<VideoCameraWrapperDelegate> delegate;

-(id)initWithDelegate:(UIViewController<VideoCameraWrapperDelegate>*)delegate andImageView:(UIImageView *)iv andFacePath:(NSString*)FacePath;
-(void)startCamera;
-(void)stopCamera;
-(void)ChangedOrintation:(CGFloat)width height:(CGFloat)height;
-(void)saveLogtoLogfile:(bool)isprintLog;

@end
