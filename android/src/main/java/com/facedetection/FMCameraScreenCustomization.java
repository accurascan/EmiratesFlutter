package com.facedetection;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

import com.facedetection.common.CameraSource;

public final class FMCameraScreenCustomization implements Parcelable {

    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_BACK = CameraSource.CAMERA_FACING_BACK;

    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_FRONT = CameraSource.CAMERA_FACING_FRONT;

    public int backGroundColor = 0xFFC4C4C5;

    public int closeIconColor = 0xFF000000;

    public int feedbackBackGroundColor = 0x00000000;
    public int feedbackTextColor = 0xFF000000;
    public int feedbackTextSize;

    public String feedBackAwayMessage;
    public String feedBackCloserMessage;
    public String feedBackOpenEyesMessage;
    public String feedBackCenterMessage;
    public String feedBackframeMessage;
    public String feedBackMultipleFaceMessage;
    public String feedBackHeadStraightMessage;
    public String feedBackBlurFaceMessage;
    public String feedBackGlareFaceMessage;
    public String feedBackLowLightMessage;
    public String feedBackProcessingMessage;
    public String feedbackDialogMessage;
    private String rawdata;
    public int showlogo = 1;
    @DrawableRes
    public int logoIcon =0;

    public int lowLightTolerence = 39;
    public int blurPercentage = 75;
    public int glareMinPercentage = 6;
    public int glareMaxPercentage = 99;
    public int facing = CAMERA_FACING_FRONT;

    public FMCameraScreenCustomization() {
    }

    protected FMCameraScreenCustomization(Parcel in) {
        backGroundColor = in.readInt();
        closeIconColor = in.readInt();
        feedbackBackGroundColor = in.readInt();
        feedbackTextColor = in.readInt();
        feedbackTextSize = in.readInt();
        showlogo = in.readInt();
        logoIcon = in.readInt();
        feedBackAwayMessage = in.readString();
        feedBackCloserMessage = in.readString();
        feedBackOpenEyesMessage = in.readString();
        feedBackCenterMessage = in.readString();
        feedBackframeMessage = in.readString();
        feedBackMultipleFaceMessage = in.readString();
        feedBackHeadStraightMessage = in.readString();
        feedBackBlurFaceMessage = in.readString();
        feedBackGlareFaceMessage = in.readString();
        feedBackLowLightMessage = in.readString();
        feedBackProcessingMessage = in.readString();
        feedbackDialogMessage = in.readString();
        lowLightTolerence = in.readInt();
        blurPercentage = in.readInt();
        glareMinPercentage = in.readInt();
        glareMaxPercentage = in.readInt();
        facing = in.readInt();
    }

    public static final Creator<FMCameraScreenCustomization> CREATOR = new Creator<FMCameraScreenCustomization>() {
        @Override
        public FMCameraScreenCustomization createFromParcel(Parcel in) {
            return new FMCameraScreenCustomization(in);
        }

        @Override
        public FMCameraScreenCustomization[] newArray(int size) {
            return new FMCameraScreenCustomization[size];
        }
    };

    public void setBlurPercentage(int blurPercentage){
        this.blurPercentage = blurPercentage;
    }

    public void setGlarePercentage(int glareMinPercentage, int glareMaxPercentage){
        this.glareMinPercentage = glareMinPercentage;
        this.glareMaxPercentage = glareMaxPercentage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(backGroundColor);
        dest.writeInt(closeIconColor);
        dest.writeInt(feedbackBackGroundColor);
        dest.writeInt(feedbackTextColor);
        dest.writeInt(feedbackTextSize);
        dest.writeInt(showlogo);
        dest.writeInt(logoIcon);
        dest.writeString(feedBackAwayMessage);
        dest.writeString(feedBackCloserMessage);
        dest.writeString(feedBackOpenEyesMessage);
        dest.writeString(feedBackCenterMessage);
        dest.writeString(feedBackframeMessage);
        dest.writeString(feedBackMultipleFaceMessage);
        dest.writeString(feedBackHeadStraightMessage);
        dest.writeString(feedBackBlurFaceMessage);
        dest.writeString(feedBackGlareFaceMessage);
        dest.writeString(feedBackLowLightMessage);
        dest.writeString(feedBackProcessingMessage);
        dest.writeString(feedbackDialogMessage);
        dest.writeInt(lowLightTolerence);
        dest.writeInt(blurPercentage);
        dest.writeInt(glareMinPercentage);
        dest.writeInt(glareMaxPercentage);
        dest.writeInt(facing);
    }

    public void setLowLightTolerence(int lowLightTolerence) {
        this.lowLightTolerence = lowLightTolerence;
    }
}
