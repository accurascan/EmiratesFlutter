package com.inet.facelock.callback;

import android.content.res.AssetManager;

public class FaceLockHelper {
    public static final int ARGB_MODE = 1;
    public static final int YUV420_MODE = 0;
    public static int IDENTIFY_THRETHOLD = 70;

    static {
        try {
            System.loadLibrary("accurafacem");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the function that initialize the face engine
     * This must be called when app is started.
     *
     * @param callback    callback to get the result
     * @param fmin        minimum distance between 2 eyes.
     * @param fmax        maximum distance between 2 eyes.
     * @param resizeRatio scale factor to detect faces.
     *                    default value is 1.18.
     *                    the smaller, the slower
     * @param modelpath   deep CNN model file's path.
     * @param weightpath  deep CNN weight file's path.
     * @return result
     */
    public static native int InitEngine(FaceCallback callback, int fmin, int fmax, float resizeRate, String modelpath, String weightpath, AssetManager assets);

    /**
     * This is the function that finalize the face engine when app is closed.
     *
     * @return
     */
    public static native boolean CloseEngine();

    /**
     * This is the function to detect faces.
     *
     * @param vBmp face image buffer
     *             its format is RGBA or YUV420
     */
    public static native void DetectLeftFace(byte[] vBmp, int width, int height);

    public static native void DetectRightFace(byte[] vBmp, int width, int height, float[] feature);

    /**
     * This is the function to extract feature from a face.
     *
     * @param vBmp       face image buffer
     *                   its format is RGBA or YUV420
     * @param pfaceRect  int array of 4 points' coordinates of face region.
     * @param plandmarks float array of face landmarks' coordinates
     *                   Look at the code in real use.
     */
    public static native void Extractfeatures(byte[] vBmp, int width, int height, float[] plandmarks, int imgType);

    /**
     * This is the function to calculate the similarity of 2 face feature vectors.
     */
    public static native float Similarity(float[] vFeat1, float[] vFeat2, int length);

}
