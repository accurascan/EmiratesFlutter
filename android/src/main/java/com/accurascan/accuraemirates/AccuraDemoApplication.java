package com.accurascan.accuraemirates;//package com.accurascan.accuraemirates;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import java.util.HashMap;
import java.util.Map;

public class AccuraDemoApplication extends Application {
    static { //for facematch
        try {
            System.loadLibrary("accurafacem");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.loadLibrary("accurasdk");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static AccuraDemoApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initFastNetwork();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    private void initFastNetwork() {
//        AndroidNetworking.initialize(getApplicationContext());
    }

    public static synchronized AccuraDemoApplication getInstance() {
        return mInstance;
    }

//    public static int getmMenuMode() {
//        return mMenuMode;
//    }

//    public static void setmMenuMode(int mMenuMode) {
//        AccuraDemoApplication.mMenuMode = mMenuMode;
//    }

    private static int mMenuMode;
//    public static int MENU_MODE_OCR = 1;
//    public static int MENU_MODE_FACE = 2;
//    public static int MENU_MODE_SCAN = 3;

    ////////////////////////
    //face match
    ////////////////////////////
    Bitmap bitmap;

    int faceCount;

//    public int getFaceCount() {
//        return this.faceCount;
//    }

//    public void setFaceCount(int c) {
//        this.faceCount = c;
//    }

//    public Bitmap getBitmap() {
//        return bitmap;
//    }

//    public void setBitmap(Bitmap bitmap) {
//        this.bitmap = bitmap;
//    }

    //ORC SCAN
    private static int refwidth;

//    public static int getRefwidth() {
//        return refwidth;
//    }

//    public static void setRefwidth(int refwidth) {
//        AccuraDemoApplication.refwidth = refwidth;
//    }

//    public static Bitmap getBarcodefacebitmap() {
//        return barcodefacebitmap;
//    }

//    public static void setBarcodefacebitmap(Bitmap barcodefacebitmap) {
//        AccuraDemoApplication.barcodefacebitmap = barcodefacebitmap;
//    }

//    private static Bitmap barcodefacebitmap;
    private static Bitmap image;
    private static Bitmap Frontimage;
    private static Bitmap CardFrontimage;

//    public static Bitmap getFaceBitmap() {
//        return FaceBitmap;
//    }
//
//    public static void setFaceBitmap(Bitmap faceBitmap) {
//        FaceBitmap = faceBitmap;
//    }

    private static Bitmap FaceBitmap;

    private static Bitmap FrontBarcodeimage;

//    public static Bitmap getFrontBarcodeimage() {
//        return FrontBarcodeimage;
//    }

//    public static void setFrontBarcodeimage(Bitmap frontBarcodeimage) {
//        FrontBarcodeimage = frontBarcodeimage;
//    }

//    public static Bitmap getBackBarcodeimage() {
//        return BackBarcodeimage;
//    }

//    public static void setBackBarcodeimage(Bitmap backBarcodeimage) {
//        BackBarcodeimage = backBarcodeimage;
//    }

//    private static Bitmap BackBarcodeimage;

//    public static Bitmap getCardFrontimage() {
//        return CardFrontimage;
//    }

//    public static void setCardFrontimage(Bitmap cardFrontimage) {
//        CardFrontimage = cardFrontimage;
//    }

//    public static Bitmap getCardBackimage() {
//        return CardBackimage;
//    }

//    public static void setCardBackimage(Bitmap cardBackimage) {
//        CardBackimage = cardBackimage;
//    }

    private static Bitmap CardBackimage;
    private static Bitmap Backimage;
    private static String[][] data;
    private static String[][] FrontData;
    private static String[][] BackData;

    public static Bitmap getFrontimage() {
        return Frontimage;
    }

    public static void setFrontimage(Bitmap frontimage) {
        Frontimage = frontimage;
    }

    public static Bitmap getBackimage() {
        return Backimage;
    }

    public static void setBackimage(Bitmap backimage) {
        Backimage = backimage;
    }

    public static String[][] getFrontData() {
        return FrontData;
    }

    public static void setFrontData(String[][] frontData) {
        FrontData = frontData;
    }

    public static String[][] getBackData() {
        return BackData;
    }

    public static void setBackData(String[][] backData) {
        BackData = backData;
    }

    private static Map<Integer, String> carddata = new HashMap<>();

    public static Map<Integer, String> getCarddata() {
        return carddata;
    }

    private static int cardId;
    private static int country_id;
    private static String countryname;

//    public static String getCountryimage() {
//        return countryimage;
//    }

//    public static void setCountryimage(String countryimage) {
//        AccuraDemoApplication.countryimage = countryimage;
//    }

//    private static String countryimage;
//
//    public static String getCountryname() {
//        return countryname;
//    }

    public static void setCountryname(String countryname) {
        AccuraDemoApplication.countryname = countryname;
    }

    public static String getCardname() {
        return cardname;
    }

    public static void setCardname(String cardname) {
        AccuraDemoApplication.cardname = cardname;
    }

    private static String cardname;

//    public static int getCountry_id() {
//        return country_id;
//    }
//
//    public static void setCountry_id(int country_id) {
//        AccuraDemoApplication.country_id = country_id;
//    }

//    public static int getCardId() {
//        return cardId;
//    }

//    public static void setCardId(int cardId) {
//        AccuraDemoApplication.cardId = cardId;
//    }

//    public static Bitmap getImage() {
//        return image;
//    }
//
//    public static void setImage(Bitmap image) {
//        AccuraDemoApplication.image = image;
//    }

    public static String[][] getData() {
        return data;
    }

    public static void setData(String[][] data) {
        AccuraDemoApplication.data = data;
    }

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

}
