#import "AccuraemiratesPlugin.h"
#if __has_include(<accuraemirates/accuraemirates-Swift.h>)
#import <accuraemirates/accuraemirates-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#endif

@implementation AccuraemiratesPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    // I need to pass this registrar variable to both ScanPreview.m and VideoCameraViewController.m
    SwiftScanPreviewViewFactory* scanPreviewViewFactory = [[SwiftScanPreviewViewFactory alloc] initWithBinaryMessenger:[registrar messenger]];
    [registrar registerViewFactory:scanPreviewViewFactory withId:@"scan_preview"];
}
@end
