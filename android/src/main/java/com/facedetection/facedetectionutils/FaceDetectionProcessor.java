package com.facedetection.facedetectionutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import com.accurascan.accuraemirates.sdk.R;
import com.accurascan.accuraemirates.R;
import com.facedetection.FMCameraScreenCustomization;
import com.facedetection.common.FrameMetadata;
import com.facedetection.common.GraphicOverlay;
import com.facedetection.utils.AccuraFaceMatchLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FaceDetectionProcessor extends VisionProcessorBase {

    static {
        System.loadLibrary("accurafacem");
    }

    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_CENTER = -7;// "Move Phone Center";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_OPEN_EYES = -4;// "Keep Your Eyes Open";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_AWAY = -3;// "Move Phone Away";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_CLOSER = -2;// "Move Phone Closer";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_FRAME_YOUR_FACE = 0;// "Frame Your Face";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_MULTIPLE_FACES = -1 ;// "Multiple face detected";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_HEAD_STEADY = -5;// "Keep Your Head Straight";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_APPROVED = 1;// "Liveness Approved";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_DARK_FACE = -8;// "-2";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_BLUR_FACE = -9;// "-2";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_GLARE = -10;// "-3";
    public static final int ACCURA_LIVENESS_FEEDBACK_CODE_INVALID = -11;// "-3";

    FaceDetectionResultListener faceDetectionResultListener;
    private FMCameraScreenCustomization livenessCustomization;

    private static final long CLICK_TIME_INTERVAL = 2000;

    private long mLastClickTime;
    private boolean takePicture = true;
    private boolean isLoaded = false;

    public FaceDetectionProcessor() {
        this.mLastClickTime = System.currentTimeMillis();
        takePicture = true;
    }

    public int initEngine(Context context){
        writeFileToPrivateStorage(context, R.raw.haarcascade_frontalface_alt, "haarcascade_frontalface_alt.xml"); //write file to private storage
        File modelFile = context.getFileStreamPath("haarcascade_frontalface_alt.xml");
        String pathModel = modelFile.getPath();

        int nRet =  initEngine(pathModel, livenessCustomization.lowLightTolerence, livenessCustomization.blurPercentage, livenessCustomization.glareMinPercentage, livenessCustomization.glareMaxPercentage);
        return nRet;
    }

    private void writeFileToPrivateStorage(Context context, int fromFile, String toFile) {
        InputStream is = context.getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try {
            FileOutputStream fos = context.openFileOutput(toFile, Context.MODE_PRIVATE);

            while ((bytes_read = is.read(buffer)) != -1)
                fos.write(buffer, 0, bytes_read); // write

            fos.close();
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FaceDetectionResultListener getFaceDetectionResultListener() {
        return faceDetectionResultListener;
    }

    public void setFaceDetectionResultListener(FaceDetectionResultListener faceDetectionResultListener) {
        this.faceDetectionResultListener = faceDetectionResultListener;
    }

    public void setCustomization(FMCameraScreenCustomization livenessCustomization) {
        this.livenessCustomization = livenessCustomization;
    }

    public boolean isTakePicture() {
        return takePicture;
    }

    public void setTakePicture(boolean takePicture) {
        this.takePicture = takePicture;
    }

    @Override
    public void stop() {
//        try {
//            detector.close();
//        } catch (Exception e) {
//            AccuraFaceMatchLog.loge(TAG, "Exception thrown while trying to close Face Detector: " + e.toString());
//        }
    }

    @Override
    protected int detectInImage(Bitmap bitmap, int i, @NonNull FrameMetadata frameMetadata) {
        // oval Rectangle
        Rect ovalRect = frameMetadata.getRect();

        //<editor-fold desc="Outer Rectangle">
        float wX = ovalRect.width() * 0.15f;
        float wY = ovalRect.height() * 0.15f;
        int left = (int) (ovalRect.left - wX);
        int top = (int) (ovalRect.top - wY);
        int right = (int) (ovalRect.right + wX);
        int bottom = (int) (ovalRect.bottom + wY);
        Rect extendOval = new Rect(left, top, right, bottom);
        if (extendOval.left < 0) extendOval.left = 0;
        if (extendOval.top < 0) extendOval.top = 0;
        if (extendOval.right > bitmap.getWidth())
            extendOval.right = bitmap.getWidth();
        if (extendOval.bottom > bitmap.getHeight())
            extendOval.bottom = bitmap.getHeight();
        frameMetadata.setRect(extendOval);
        //</editor-fold>
        return detectFace(bitmap, i, ovalRect.left, ovalRect.top, ovalRect.width(), ovalRect.height());
    }

//    @Override
//    protected Task<List<Face>> detectInImage(InputImage image) {
//        return detector.process(image);
//    }

    @Override
    protected void onSuccess(@Nullable  Bitmap originalCameraImage, int results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
        if (originalCameraImage == null)
            return;

        AccuraFaceMatchLog.loge(TAG, "onSuccess");
        if (results == 1) {
            faceDetectionResultListener.onSuccess(originalCameraImage, frameMetadata,graphicOverlay);
        } else {
            faceDetectionResultListener.onFeedBackMessage(results);
            if(!originalCameraImage.isRecycled()) originalCameraImage.recycle();
        }

    }

    @Override
    protected void onFailure(@NonNull Exception e) {

        if (faceDetectionResultListener != null)
            faceDetectionResultListener.onFailure(e);
    }

    private native int initEngine(String a, int lightTolerance, int faceBlurPercentage, int minPercentage, int maxPercentage);
    private native int detectFace(Bitmap bitmap, int i, int left, int top, int right, int bottom);
}
