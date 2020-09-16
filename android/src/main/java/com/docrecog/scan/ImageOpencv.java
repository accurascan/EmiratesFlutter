package com.docrecog.scan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class ImageOpencv {
    public String message = "";
    public boolean isSucess = false;
    public boolean isChangeCard = false;
    public int cardPos = 0;
    public int responseCode = 0;
    public String[][] finalData = null;
    public String rect;
    public OpenCvHelper.AccuraOCR accuraOCR = OpenCvHelper.AccuraOCR.NONE;
    public Mat croppedPick;

    public static Bitmap getImage(String s) {
        if (!s.equals("")) {
            try {
                byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);//memory leak
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public Bitmap getBitmap() {
        if (croppedPick != null && croppedPick.width() > 0 && croppedPick.height() > 0) {
            Bitmap bmp = Bitmap.createBitmap(croppedPick.width(), croppedPick.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(croppedPick, bmp);
            croppedPick.release();
            return bmp;
        }
        return null;
    }
}
