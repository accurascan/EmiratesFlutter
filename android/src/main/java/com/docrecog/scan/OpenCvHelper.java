package com.docrecog.scan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.motiondetection.data.GlobalData;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCvHelper {

    //    private Mat template;
    private String TAG = "OpenCvHelper";
    private String newMessage = "";

    private OCRCallback faceMatchCallBack;

//    public void setTemplate(Mat outMat) {
//        if (outMat != null) {
//            if (template!=null) {
//                template.release();
//            }
//            template = outMat;
//        }
//    }

    public enum AccuraOCR {
        SUCCESS,
        FAIL,
        NONE
    }

    public OpenCvHelper(Context context) {
        if (context instanceof OCRCallback) {
            this.faceMatchCallBack = (OCRCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + OCRCallback.class.getName());
        }
    }

    public ImageOpencv nativeCheckCardIsInFrame(final Context context, Bitmap bmp, boolean doblurcheck) {

        Mat clone = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp, clone);
        Mat outMat = new Mat();

        ImageOpencv i = RecogEngine.checkCardInFrames(clone.getNativeObjAddr(), outMat.getNativeObjAddr(), doblurcheck);
        if (i != null) {
            i.accuraOCR = i.isSucess ? AccuraOCR.SUCCESS : AccuraOCR.FAIL;
//                return i;
            if (i.isChangeCard && i.cardPos > 0) {
                faceMatchCallBack.onUpdateProcess(getErrorMessage(i.message));
//                return i.isSucess;
            } else {
                newMessage = i.message;
                if (!i.message.equals("")) {
                    faceMatchCallBack.onUpdateProcess(getErrorMessage(i.message));
                }
            }
            if (i.isSucess) {
                i.croppedPick = new Mat();
                outMat.copyTo(i.croppedPick);
                outMat.release();
            }
//            if (!i.message.equals("")) {
//                ImageOpencv finalI = i;
//                final Runnable runnable = new Runnable() {
//                    public void run() {
//                        try {
//                            if (!finalI.message.contains("lighting")) {
//                                if (newMessage.equals(finalI.message) || !finalI.message.contains("Process")) {
//                                    faceMatchCallBack.onUpdateProcess("");
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                Runnable runnable1 = new Runnable() {
//                    public void run() {
//                        new Handler().postDelayed(runnable, 1000);
//                    }
//                };
//                ((Activity) context).runOnUiThread(runnable1);
//            }

        } else {
            i = new ImageOpencv();
            i.accuraOCR = AccuraOCR.NONE;
        }
        try {
            clone.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    int frameCount = 0;

    public int doCheckFrame(byte[] data, int width, int height) {
        frameCount++;
        if (GlobalData.isPhoneInMotion()) {
            if (frameCount % 5 == 0) {
                faceMatchCallBack.onUpdateProcess(getErrorMessage("0"));
            }
            return 0;
        }
        int i = RecogEngine.doCheckData(data, width, height);
        if (faceMatchCallBack != null) {
            if (i == 0) {
                if (frameCount % 2 == 0) faceMatchCallBack.onUpdateProcess(getErrorMessage("0"));
            } else if (i > 0 && newMessage.equals("0")) {
                faceMatchCallBack.onUpdateProcess("");
            }
        }
        return i;
    }

    /**
     * Developer can mange there custom messages
     *
     * @param s message code coming from SDK
     * @return message which display on ui
     */
    public String getErrorMessage(String s) {
        newMessage = s;
        return s;
    }

}
