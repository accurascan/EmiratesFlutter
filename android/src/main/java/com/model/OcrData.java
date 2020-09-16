package com.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.docrecog.scan.RecogResult;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.Serializable;

public class OcrData implements Serializable {

    private String cardname;
    private String countryname;
    private transient Bitmap Backimage; // transient filed does not serialized
    public String backDocument;
    private String[][] BackData;
    private transient Bitmap Frontimage; // transient filed does not serialized
    public String frontDocument;
    private String[][] FrontData;
    private RecogResult mrzData;

    public String getCardname() {
        return cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }

    public Bitmap getBackimage() {
        if (Backimage != null && !Backimage.isRecycled()) {
            return Backimage;
        } else if (!TextUtils.isEmpty(backDocument)) {
            try {

                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(backDocument, bmpFactoryOptions);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public void setBackimage(Bitmap backimage) {
        Backimage = backimage;
    }

    public void setBackimage(Bitmap backimage, File cacheFile) {
        Mat mat = new Mat();
        Utils.bitmapToMat(backimage, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        backDocument = new File(cacheFile, "backImage.jpg").getAbsolutePath();
        Imgcodecs.imwrite(backDocument, mat);
    }

    public String[][] getBackData() {
        return BackData;
    }

    public void setBackData(String[][] backData) {
        BackData = backData;
    }

    public Bitmap getFrontimage() {
        if (Frontimage != null && !Frontimage.isRecycled()) {
            return Frontimage;
        } else if (!TextUtils.isEmpty(frontDocument)) {
            try {
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(frontDocument, bmpFactoryOptions);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public void setFrontimage(Bitmap frontimage) {
        Frontimage = frontimage;
    }

    public void setFrontimage(Bitmap frontimage, File cacheFile) {
        Mat mat = new Mat();
        Utils.bitmapToMat(frontimage, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        frontDocument = new File(cacheFile, "frontImage.jpg").getAbsolutePath();
        Imgcodecs.imwrite(frontDocument, mat);
    }

    public String[][] getFrontData() {
        return FrontData;
    }

    public void setFrontData(String[][] frontData) {
        FrontData = frontData;
    }

    public RecogResult getMrzData() {
        return mrzData;
    }

    public void setMrzData(RecogResult mrzData) {
        this.mrzData = mrzData;
    }

}
