package com.docrecog.scan;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.opencv.core.Size;

public class CameraPreview extends FrameLayout {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CameraActivity activity;
    private final SurfaceView surfaceView;

    public CameraPreview(CameraActivity cameraActivity, Context context, Camera camera) {
        super(context);
        mCamera = camera;

        surfaceView = new SurfaceView(context);
        addView(surfaceView);
        mHolder = surfaceView.getHolder();

        activity = cameraActivity;
//        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    //    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        refreshCamera(mCamera);
    }

    //    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // To Set preview for camera ratio
        // The isPreviewSet will be false then update surface view to maintain camera ratio for all device

        int width = activity.mPreviewWidth, height = activity.mPreviewHeight;
        if (activity.mCameraDevice != null) {
            Size size = activity.previewSize;
            if (size != null) {
                width = (int) size.width;
                height = (int) size.height;
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
//        if (isPortraitMode()) {
        int tmp = width;
        width = height;
        height = tmp;
//        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) layoutWidth / (float) width;
        float heightRatio = (float) layoutHeight / (float) height;

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = layoutWidth;
            childHeight = (int) ((float) height * widthRatio);
            childYOffset = (childHeight - layoutHeight) / 2;
        } else {
            childWidth = (int) ((float) width * heightRatio);
            childHeight = layoutHeight;
            childXOffset = (childWidth - layoutWidth) / 2;
        }
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            params.setMargins(-1 * childXOffset, -1 * childYOffset, -1 * childXOffset, -1 * childYOffset);
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
    }

    public SurfaceHolder getHolder() {
        return surfaceView.getHolder();
    }

//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        // TODO Auto-generated method stub
//        // mCamera.release();
//
//    }
}