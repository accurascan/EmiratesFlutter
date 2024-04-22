package com.facedetection.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.facedetection.utils.AccuraFaceMatchLog;

import java.io.IOException;

/**
 * Preview the camera image in the screen.
 */
public class CameraSourcePreview extends FrameLayout {
    private static final String TAG = "Preview";

    private final Context context;
    private final SurfaceView surfaceView;
    private boolean startRequested;
    private boolean surfaceAvailable;
    private CameraSource cameraSource;

    private GraphicOverlay overlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        startRequested = false;
        surfaceAvailable = false;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    private void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        this.cameraSource = cameraSource;

        if (this.cameraSource != null) {
            startRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        this.overlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
//        surfaceView.getHolder().getSurface().release();
    }

    @SuppressLint("MissingPermission")
    private void startIfReady() throws IOException {
        if (startRequested && surfaceAvailable) {
            cameraSource.start(surfaceView.getHolder());
            requestLayout();

            if (overlay != null) {
                Size size = cameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    overlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
                } else {
                    overlay.setCameraInfo(max, min, cameraSource.getCameraFacing());
                }
                overlay.clear();
            }
            startRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                AccuraFaceMatchLog.loge(TAG, "Could not start camera source. " + e.toString());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            surfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (cameraSource != null) cameraSource.initializeFirstTime();
        }
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if (isInEditMode()) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            return;
//        }
//        // Handle android:adjustViewBounds
//        if (true) {
//            if (!cameraSource.isCameraOpened()) {
//                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//                return;
//            }
//            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
//                final AspectRatio ratio = AspectRatio.of(cameraSource.getPreviewSize().getWidth(), cameraSource.getPreviewSize().getHeight());
//                assert ratio != null;
//                int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat());
//                if (heightMode == MeasureSpec.AT_MOST) {
//                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
//                }
//                super.onMeasure(widthMeasureSpec,
//                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
//            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
//                final AspectRatio ratio = AspectRatio.of(cameraSource.getPreviewSize().getWidth(), cameraSource.getPreviewSize().getHeight());
//                assert ratio != null;
//                int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat());
//                if (widthMode == MeasureSpec.AT_MOST) {
//                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
//                }
//                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
//                        heightMeasureSpec);
//            } else {
//                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            }
//        } else {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
//        // Measure the TextureView
//        int width = getMeasuredWidth();
//        int height = getMeasuredHeight();
//        AspectRatio ratio =  AspectRatio.of(cameraSource.getPreviewSize().getWidth(), cameraSource.getPreviewSize().getHeight());
////        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
////            ratio = ratio.inverse();
////        }
//        assert ratio != null;
//        if (height < (width * ratio.getY()) / ratio.getX()) {
//            measure(
//                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
//                    MeasureSpec.makeMeasureSpec(width * ratio.getY() / ratio.getX(),
//                            MeasureSpec.EXACTLY));
//        } else {
//            measure(
//                    MeasureSpec.makeMeasureSpec(height * ratio.getX() / ratio.getY(),
//                            MeasureSpec.EXACTLY),
//                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
//        }
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;
        if (cameraSource != null) {
            Size size = cameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int) (((float) layoutWidth / (float) width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) height) * width);
        }

        float rleft = 0;
        float rtop = 0;
        float rright = childWidth;
        float rbottom = childHeight;

        float x = layoutWidth / 2.0f;
        float y = layoutHeight / 2.0f;

        float xOffset = (childWidth / 2.0f);
        float yOffset = (childHeight / 2.0f);
        if (childWidth < layoutWidth) {
            rleft = x - xOffset;
            rright = x + xOffset;
        }

        if (childHeight < layoutHeight) {
            rtop = y - yOffset;
            rbottom = y + yOffset;
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout((int) rleft,(int)  rtop,(int)  rright,(int)  rbottom);
        }


//        int childWidth;
//        int childHeight;
//        int childXOffset = 0;
//        int childYOffset = 0;
//        float widthRatio = (float) layoutWidth / (float) width;
//        float heightRatio = (float) layoutHeight / (float) height;
//
//        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
//        // it is usually necessary to slightly oversize the child and to crop off portions along one
//        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
//        // compute a crop offset for the other dimension.
//        if (widthRatio > heightRatio) {
//            childWidth = layoutWidth;
//            childHeight = (int) ((float) height * widthRatio);
//            childYOffset = (childHeight - layoutHeight) / 2;
//        } else {
//            childWidth = (int) ((float) width * heightRatio);
//            childHeight = layoutHeight;
//            childXOffset = (childWidth - layoutWidth) / 2;
//        }
//
//        for (int i = 0; i < getChildCount(); ++i) {
//            // One dimension will be cropped.  We shift child over or up by this offset and adjust
//            // the size to maintain the proper aspect ratio.
//            getChildAt(i).layout(
//                    -1 * childXOffset, -1 * childYOffset,
//                    childWidth - childXOffset, childHeight - childYOffset);
//        }

        try {
            startIfReady();
        } catch (IOException e) {
            AccuraFaceMatchLog.loge(TAG, "Could not start camera source. " + e.toString());
        }
    }

    private boolean isPortraitMode() {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }
        return false;
    }
}