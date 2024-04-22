package com.facedetection.facedetectionutils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import com.accurascan.accuraemirates.sdk.BuildConfig;
import com.accurascan.accuraemirates.BuildConfig;
import com.facedetection.common.BitmapUtils;
import com.facedetection.common.FrameMetadata;
import com.facedetection.common.GraphicOverlay;
import com.facedetection.common.VisionImageProcessor;
import com.facedetection.utils.AccuraFaceMatchLog;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Abstract base class for frame processors. Subclasses need to implement {@link
 * #onSuccess(Bitmap, int, FrameMetadata, GraphicOverlay)} to define what they want to with
 * the detection results and {@link #detectInImage(Bitmap, int, FrameMetadata)} to specify the detector
 * object.
 *
 */
public abstract class VisionProcessorBase implements VisionImageProcessor {

    final String TAG = "Processor";

    public final AtomicBoolean isSmother = new AtomicBoolean(false);
    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")
    private FrameMetadata processingMetaData;

    int width = 0, height = 0;
    DisplayMetrics dm;

    public VisionProcessorBase() {
        dm = Resources.getSystem().getDisplayMetrics();
        width = (int) (dm.widthPixels / BitmapUtils.WIDTH_RATIO);
        height = (int) (width * BitmapUtils.HEIGHT_RATIO);
    }

    @Override
    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) throws Exception {
//        Logger.e(TAG, "countFrame :-> " + countFrame++);
//        if (!this.isSmother.get()) {
//            processImage(data, frameMetadata, graphicOverlay);
//            Logger.e(TAG, "count :-> " + count++);
            latestImage = data;
            latestImageMetaData = frameMetadata;
            if (processingImage == null && processingMetaData == null) {
                processLatestImage(graphicOverlay);
            }
//        }
    }

    @Override
    public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {
//        if (!this.isSmother.get())
//            detectInVisionImage(null /* bitmap */, InputImage.fromBitmap(bitmap, 0), null,
//                    graphicOverlay);
    }

    private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata,
            final GraphicOverlay graphicOverlay) {

        // To create the Image

        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
        if (bitmap == null) {
            return;
        }
        int croppedWidth = width;
        int croppedHeight = height;
        Point centerOfCanvas = new Point(dm.widthPixels / 2, dm.heightPixels / 2);
        int left = centerOfCanvas.x - (croppedWidth / 2);
        int top = centerOfCanvas.y - (croppedHeight / 2);
        int right = centerOfCanvas.x + (croppedWidth / 2);
        int bottom = centerOfCanvas.y + (croppedHeight / 2);
        Rect frameRect = new Rect(left, top, right, bottom);
        float widthScaleFactor = (float) dm.widthPixels / (float) bitmap.getWidth();
        float heightScaleFactor = (float) (dm.heightPixels) / (float) bitmap.getHeight();
//        float finalWidth = frameRect.width()*widthScaleFactor;
//        float finalHeight = frameRect.height()*heightScaleFactor;
        frameRect.left = (int) (frameRect.left / widthScaleFactor);
        frameRect.right = (int) (frameRect.right / widthScaleFactor);
//        frameRect.top = (int) (frameRect.top / heightScaleFactor);
//        frameRect.bottom = (int) (frameRect.bottom / heightScaleFactor);
        float heightOffset = ((frameRect.right-frameRect.left) * BitmapUtils.HEIGHT_RATIO) / 2;
        frameRect.top = (int) ((bitmap.getHeight() * 0.5f) - heightOffset);
        frameRect.bottom = (int) ((bitmap.getHeight() * 0.5f) + heightOffset);
        Rect finalrect = new Rect((int) (frameRect.left), (int) (frameRect.top), (int) (frameRect.right), (int) (frameRect.bottom));

        try {
            int l1 = Math.max(finalrect.left, 0);
            int t1 = Math.max(finalrect.top, 0);
            int w = Math.min(finalrect.width(), bitmap.getWidth());
            int h = Math.min(finalrect.height(), bitmap.getHeight());
//            Logger.e("TAG", "procesImage:around " + "Rect(" + l1 + ", " + t1 + " - " + (l1 + w) + ", " + (t1 + h) + ")" + " (w,h) : (" + w + "," + h + ")");
//            Bitmap bmCard = Bitmap.createBitmap(bitmap, l1,t1,w,h);
//            frameMetadata.setBitmap(bmCard);
            frameMetadata.setRect(new Rect(l1,t1,l1+w,t1+h));
        } catch (Exception e) {
            AccuraFaceMatchLog.loge(TAG, "Process " + e.toString());
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        detectInVisionImage(
                bitmap, frameMetadata,
                graphicOverlay);

    }

    private void detectInVisionImage(
            final Bitmap originalCameraImage,
            final FrameMetadata metadata, final GraphicOverlay graphicOverlay){

        this.isSmother.set(true);
        int i = detectInImage(originalCameraImage, 0, metadata);

        if (i > 0)
            i = FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_APPROVED;

        VisionProcessorBase.this.onSuccess(originalCameraImage, i,
                metadata,
                graphicOverlay);

        if (i!=1)processLatestImage(graphicOverlay);
        VisionProcessorBase.this.isSmother.set(false);

    }

    @Override
    public void stop() {
    }

    protected abstract int detectInImage(Bitmap bitmap, int i, @NonNull FrameMetadata frameMetadata);

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     *                            image.
     */
    protected abstract void onSuccess(
            @Nullable Bitmap originalCameraImage,
            int results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);
}
