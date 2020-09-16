package com.inet.facelock.callback;


public interface FaceCallback {

  /**
   * This is called after initialized face detection engine.
   * @param ret
   */
  public void onInitEngine(int ret);
  /**
   * This is callback function to get face detection result.
   * @param faceArray
   */
  public void onLeftDetect(FaceDetectionResult face);
  public void onRightDetect(FaceDetectionResult face);
  /**
   * This is called after initialized feature extraction engine.
   * @param ret
   */
  public void onExtractInit(int ret);
  /**
   * This is callback function to get face feature.
   * @param faceFeature
   */
}
