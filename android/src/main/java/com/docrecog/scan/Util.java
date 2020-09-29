/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.docrecog.scan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.accurascan.accuraemirates.BuildConfig;
import com.accurascan.accuraemirates.R;
import com.accurascan.accuraemirates.camera.CameraHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static androidx.core.content.ContextCompat.checkSelfPermission;

/**
 * Collection of utility functions used in this package.
 */
public class Util {
    private static final String TAG = "Util";

    // The brightness setting used when it is set to automatic in the system.
    // The reason why it is set to 0.7 is just because 1.0 is too bright.
    // Use the same setting among the Camera, VideoCamera and Panorama modes.
    private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;

    private static final int OPEN_RETRY_COUNT = 2;

    // Orientation hysteresis amount used in rounding, in degrees
    public static final int ORIENTATION_HYSTERESIS = 5;


    private Util() {

    }

    public static void Assert(boolean cond) {
//        try {
//            if (!cond) {
//                throw new AssertionError();
//            }
//        }catch (Exception e){
//
//        }
    }


    public static Size getMaxPictureSize(List<Size> supported, int camOri) {
        Size maxSize = supported.get(0);

        if (camOri == 0 || camOri == 180) {
            for (Size size : supported) {
                if (size.height > maxSize.height)
                    maxSize = size;
            }
        } else {
            for (Size size : supported) {
                if (size.width > maxSize.width)
                    maxSize = size;
            }
        }
        return maxSize;
    }

    public static boolean isPermissionsGranted(Context context) {
        for (String permission : getRequiredPermissions(context)) {
            if (checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static String[] getRequiredPermissions(Context context) {
        String[] ps = new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
//        try {
//            PackageInfo info =
//                    context
//                            .getPackageManager()
//                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
//            String[] ps = info.requestedPermissions;
//            return (ps != null && ps.length > 0) ? ps : new String[0];
//        } catch (Exception e) {
//            return new String[0];
//        }

        return ps;
    }

    public static Camera openCamera(int cameraId)
            throws Exception {
        // Check if device policy has disabled the camera.
//        if (!activity.isFinishing()) {
//            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(
//                    Context.DEVICE_POLICY_SERVICE);
//            if (dpm.getCameraDisabled(null) == true) {
//                throw new Exception();
//            }
            for (int i = 0; i < OPEN_RETRY_COUNT; i++) {
                try {
                    return CameraHolder.instance().open(cameraId);
                } catch (Exception e) {
                    if (i == 0) {
                        try {
                            //wait some time, and try another time
                            //Camera device may be using by VT or atv.
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                        }
                        continue;
                    } else {
                        // In eng build, we throw the exception so that test tool
                        // can detect it and report it
                        if ("eng".equals(Build.TYPE)) {
                            if (BuildConfig.DEBUG) Log.i(TAG, "Open Camera fail", e);
                            throw e;
                            //QA will always consider JE as bug, so..
                            //throw new RuntimeException("openCamera failed", e);
                        } else {
                            throw e;
                        }
                    }
                }
            }
            //just for build pass
//        }
        throw new Exception(new RuntimeException("Should never get here"));

    }


    public static boolean equals(Object a, Object b) {
        return (a == b) || (a == null ? false : a.equals(b));
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }

    public static double[] getPreviewRatio(View realView, Parameters paramaters) {
        int viewW = realView.getWidth();
        int viewH = realView.getHeight();
        int previewW = paramaters.getPreviewSize().width;
        int previewH = paramaters.getPreviewSize().height;
        double scaleX = (double) viewW / (double) previewW;
        double scaleY = (double) viewH / (double) previewH;
        double[] ratio = new double[2];
        ratio[0] = scaleX;
        ratio[1] = scaleY;
        return ratio;
    }


    public static int getDisplayRotation(Activity activity) {
        if (!activity.isFinishing()) {
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    return 0;
                case Surface.ROTATION_90:
                    return 90;
                case Surface.ROTATION_180:
                    return 180;
                case Surface.ROTATION_270:
                    return 270;
            }
        }
        return 0;
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static int getCameraOrientation(int cameraId) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    public static Size getOptimalPreviewSize(Activity currentActivity,
                                             List<Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        double maxDiff = 0;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        Display display = currentActivity.getWindowManager().getDefaultDisplay();

        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            // We don't know the size of SurfaceView, use screen height
            targetHeight = display.getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
//            if (Math.abs(size.height - targetHeight) > maxDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
//                maxDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            maxDiff = 0;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
//            	if (Math.abs(size.height - targetHeight) > maxDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
//                    maxDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static Size getOptimalPreviewSizeByArea(List<Size> sizes, int requiredArea) {
        // Use a very small tolerance because we want an exact match.
        if (sizes == null) return null;
        Size optimalSize = null;
        int minDiff = Integer.MAX_VALUE;
        // Try to find an size match aspect ratio and size
        if (BuildConfig.DEBUG) Log.e(TAG, "getOptimalPreviewSizeByArea: " + minDiff);
        for (Size size : sizes) {
            int area = size.width * size.height;
            int diff = Math.abs(requiredArea - area);
            if (diff < minDiff) {
                optimalSize = size;
                minDiff = diff;
            }
        }
        return optimalSize;
    }

    public static Size getMaxPreviewSizeWithRatio(Activity currentActivity,
                                                  List<Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;

        Size maxPreviewSize = null;
        double maxDiff = 0;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        Display display = currentActivity.getWindowManager().getDefaultDisplay();

        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            // We don't know the size of SurfaceView, use screen height
            targetHeight = display.getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) > maxDiff) {
                maxPreviewSize = size;
                maxDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (maxPreviewSize == null) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No preview size match the aspect ratio");
            maxDiff = 0;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) > maxDiff) {
                    maxPreviewSize = size;
                    maxDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return maxPreviewSize;
    }

    public static Size getMaxPreviewSize(List<Size> supported, int camOri) {
        Size maxSize = supported.get(9);

//		if (camOri == 0 || camOri == 180) {
//			for (Camera.Size size : supported) {
//				if (size.height > maxSize.height)
//					maxSize = size;
//			}
//		} else {
//			for (Camera.Size size : supported) {
//				if (size.width > maxSize.width)
//					maxSize = size;
//			}
//		}
        return maxSize;
    }


    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                                     int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }


    public static void initializeScreenBrightness(Window win, ContentResolver resolver) {
        // Overright the brightness settings if it is automatic
        int mode = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
            win.setAttributes(winParams);
        }
    }


    public static void showErrorAndFinish(final Activity activity, int msgId) {
        if (!activity.isFinishing()) {
            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    };
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    //.setTitle(R.string.camera_error_title)
                    .setTitle("")
                    .setMessage(msgId)
                    .setNeutralButton(R.string.dialog_ok, buttonListener)
                    .show();
        }
    }

    public static String writeBitmap2File(Bitmap bitmap, int mode) {
        ByteArrayOutputStream baos = null;
        String _subdir = null;
        String _fname = null;
        String _path = null;
        try {
            if (mode == 0)
                _subdir = "/aPassportOCR/PreviewImages/";
            else
                _subdir = "/aPassportOCR/PictureImages/";
            File dir = new File(Environment.getExternalStorageDirectory().toString(), _subdir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    if (BuildConfig.DEBUG) Log.d("App", "failed to create directory");
                    return "";
                }
            }

            _fname = getCurTimeString() + ".jpg";
            _path = dir.getAbsolutePath() + _fname;
            baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 95, baos);
            byte[] photoBytes = baos.toByteArray();
            if (photoBytes != null) {
                new FileOutputStream(new File(dir.getAbsolutePath(), _fname)).write(photoBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _path;
    }

    public static String getCurTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());

        return currentDateandTime;
    }
}


