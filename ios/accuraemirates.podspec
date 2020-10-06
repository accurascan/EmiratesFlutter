#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint accuraemirates.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'accuraemirates'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin.'
  s.description      = <<-DESC
A new Flutter plugin.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*{.h,.a,.swift,.mm,.m,.hpp}'
  s.resources = 'Resources/**/*'
  s.dependency 'Flutter'
  s.static_framework = true
  s.public_header_files = "Classes/VideoCamera/VideoCameraWrapperDelegate.h", "Classes/VideoCamera/VideoCameraWrapper.h", 'Classes/*.h'
  s.private_header_files = 'Classes/CodeScan/**/*{.h,.cpp,.hpp}', 'Classes/FaceMatch/**/*{.h,.cpp,.hpp}'
  s.platform = :ios, '8.0'
  s.preserve_paths = 'opencv2.framework', 'Classes/Framework/*.a'
  s.xcconfig = {
    'OTHER_LDFLAGS' => '-framework opencv2 -lc++ -lAccuraFace -lAccuraEmirate -lz',
    'USER_HEADER_SEARCH_PATHS' => '"${PROJECT_DIR}/.."/**',
    "LIBRARY_SEARCH_PATHS" => '"${PROJECT_DIR}/.symlinks/plugins/accuraemirates/ios/Classes/.."/**',
  }
  s.vendored_frameworks = 'opencv2.framework', "CoreVideo.framework", "Foundation.framework", "CoreGrpahics.framework",
  "Accelerate.framework", "CoreMedia.framework", "CoreImage.framework", "QuartzCore.framework", "AudioToolbox.framework",
  "CoreData.framework", "SystemConfiguration.framework", "AVFoundation.framework"
  s.vendored_libraries = 'libAccuraFace','libAccuraEmirate'

  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
  # s.swift_version = '5.0'
end
