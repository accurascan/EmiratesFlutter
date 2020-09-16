package com.inet.facelock.callback;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class FaceDetectionResult {
    Rect   faceRect;
    float  confidence;
    int		newWidth;
    int		newHeight;
    byte[]	newImg;
    float[] feature;

    public int getNewWidth() {
        return newWidth;
    }

    public void setNewWidth(int newWidth) {
        this.newWidth = newWidth;
    }

    public int getNewHeight() {
        return newHeight;
    }

    public void setNewHeight(int newHeight) {
        this.newHeight = newHeight;
    }

    public byte[] getNewImg() {
        return newImg;
    }

    public void setNewImg(byte[] newImg) {
        this.newImg = newImg;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public Rect getFaceRect() {
        return faceRect;
    }

    public void setFaceRect(int left, int top, int right, int bottom) {
        this.faceRect = new Rect(left, top,right,bottom);
    }

    public float[] getFeature() {
        return feature;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
    }

    public Bitmap getFaceImage(Bitmap bitmap) {
        float w = faceRect.width() * 0.12f;
        float h = faceRect.height() * 0.12f;
        int newX = faceRect.left - (int) w;
        int newY = faceRect.top - (int) h;
        int newWidth = (int) (faceRect.width() + (w * 2));
        int newHeight = (int) (faceRect.height() + (h * 2));
        if (newX < 0) {
            newWidth = newWidth + (int) (newX * 2);
            newX = 0;
        }
        if (newY < 0) {
            newHeight = newHeight + (int) (newY * 2);
            newY = 0;
        }
        if (newX + newWidth > bitmap.getWidth()) newWidth = bitmap.getWidth() - newX;
        if (newY + newHeight > bitmap.getHeight()) newHeight = bitmap.getHeight() - newY;
        Rect rect = new Rect(newX, newY, newX + newWidth, newY + newHeight);
        try {
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top,
                    rect.width(),
                    rect.height());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
