package com.facedetection.facedetectionutils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facedetection.common.FrameMetadata;
import com.facedetection.common.GraphicOverlay;

public interface FaceDetectionResultListener {
    void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    void onFailure(@NonNull Exception e);

    void onFeedBackMessage(int s);
}
