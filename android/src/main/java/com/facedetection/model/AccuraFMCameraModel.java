package com.facedetection.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import java.io.File;

@Keep
public final class AccuraFMCameraModel implements Parcelable {

    private String status;
    private byte[] faceBiometric;
    protected Uri uri;

    public AccuraFMCameraModel() {
    }

    protected AccuraFMCameraModel(Parcel in) {
        status = in.readString();
        faceBiometric = in.createByteArray();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<AccuraFMCameraModel> CREATOR = new Creator<AccuraFMCameraModel>() {
        @Override
        public AccuraFMCameraModel createFromParcel(Parcel in) {
            return new AccuraFMCameraModel(in);
        }

        @Override
        public AccuraFMCameraModel[] newArray(int size) {
            return new AccuraFMCameraModel[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Bitmap getFaceBiometrics() {
//        if (faceBiometric != null && faceBiometric.length > 0) {
//            return BitmapFactory.decodeByteArray(faceBiometric, 0, faceBiometric.length);
//        }
        return getFaceBiometric();
    }

    public byte[] getArray() {
        return faceBiometric;
    }

    public void setFaceBiometrics(byte[] bytes) {
        this.faceBiometric = bytes;
    }

    private Bitmap getFaceBiometric() {
        if (uri != null) {
            File file = new File(this.uri.getPath());
            return BitmapFactory.decodeFile(file.getPath());
        } else return null;
    }

    public void setFaceBiometric(Uri faceUri) {
        this.uri = faceUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeByteArray(faceBiometric);
        dest.writeParcelable(uri, flags);
    }

}
