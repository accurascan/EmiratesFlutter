package com.accurascan.accuraemirates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.docrecog.scan.CameraActivity;
import com.docrecog.scan.FaceMatch;
import com.docrecog.scan.RecogEngine;
import com.docrecog.scan.Util;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceLockHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;

public class MainActivity extends FlutterActivity implements FaceCallback {
    FaceDetectionResult leftResult = null;
    FaceDetectionResult rightResult = null;
    float match_score = 0.0f;
    private boolean faceResult_match = false;
    private static RecogEngine mCardScanner;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Util.isPermissionsGranted(MainActivity.this)) {
                    requestCameraPermission();
                } else {

                    if (mCardScanner == null) {
                        mCardScanner = new RecogEngine();
                        mCardScanner.initEngine(MainActivity.this,MainActivity.this);
                    }
                    initEngine();//for face match
                }
            }
        }, 3000);
    }

    private void initEngine() {

        //call Sdk  method InitEngine
        // parameter to pass : FaceCallback callback, int fmin, int fmax, float resizeRate, String modelpath, String weightpath, AssetManager assets
        // this method will return the integer value
        //  the return value by initEngine used the identify the particular error
        // -1 - No key found
        // -2 - Invalid Key
        // -3 - Invalid Platform
        // -4 - Invalid License

        writeFileToPrivateStorage(R.raw.model, "model.prototxt"); //write file to private storage
        File modelFile = getApplicationContext().getFileStreamPath("model.prototxt");
        String pathModel = modelFile.getPath();
        writeFileToPrivateStorage(R.raw.weight, "weight.dat");
        File weightFile = getApplicationContext().getFileStreamPath("weight.dat");
        String pathWeight = weightFile.getPath();

        int nRet = FaceLockHelper.InitEngine(this, 30, 800, 1.18f, pathModel, pathWeight, this.getAssets());
        Log.i("ViewDataActivityTEMP", "InitEngine: " + nRet);
        if (nRet < 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            if (nRet == -1) {
                builder1.setMessage("No Key Found");
            } else if (nRet == -2) {
                builder1.setMessage("Invalid Key");
            } else if (nRet == -3) {
                builder1.setMessage("Invalid Platform");
            } else if (nRet == -4) {
                builder1.setMessage("Invalid License");
            }

            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    //Copy file to privateStorage
    public void writeFileToPrivateStorage(int fromFile, String toFile) {
        InputStream is = getApplicationContext().getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput(toFile, Context.MODE_PRIVATE);

            while ((bytes_read = is.read(buffer)) != -1)
                fos.write(buffer, 0, bytes_read); // write

            fos.close();
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitEngine(int ret) {
    }

    // call if face detect
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        leftResult = null;
        if (faceResult != null) {
            leftResult = faceResult;

            if (FaceMatch.face2 != null && !FaceMatch.face2.isRecycled()) {
                Bitmap nBmp = FaceMatch.face2.copy(Bitmap.Config.ARGB_8888, true);
                if (nBmp != null && !nBmp.isRecycled()) {
                    int w = nBmp.getWidth();
                    int h = nBmp.getHeight();
                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);
                    if (leftResult != null) {
                        FaceLockHelper.DetectRightFace(buff.array(), w, h, leftResult.getFeature());
                    } else {
                        FaceLockHelper.DetectRightFace(buff.array(), w, h, null);
                    }
                    CameraActivity.leftResult = leftResult;
                }
            }
        }
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {

        if (faceResult != null) {
            rightResult = faceResult;
            Bitmap facecrop = rightResult.getFaceImage(FaceMatch.face2);
            CameraActivity.rightResult = rightResult;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            facecrop.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String BackImageencoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            CameraActivity.facematch_resutl.success(BackImageencoded);
            faceResult_match = true;
        } else {
            rightResult = null;
            CameraActivity.rightResult = rightResult;
            CameraActivity.facematch_resutl.success("0.0");
            faceResult_match = false;
        }
    }

    @Override
    public void onExtractInit(int ret) {
    }

    //Class use for creating a view
    public class MyView extends View {
        Bitmap image = null;
        FaceDetectionResult detectionResult = null;

        @SuppressLint("SdCardPath")
        public MyView(Context context) {
            super(context);

            @SuppressWarnings("deprecation")
            ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);

            this.setLayoutParams(param);
        }

        public void setImage(Bitmap image) {
            this.image = Bitmap.createBitmap(image);
        }

        public Bitmap getImage() {
            return image;
        }

        public void setFaceDetectionResult(FaceDetectionResult result) {
            this.detectionResult = result;
        }

        public FaceDetectionResult getFaceDetectionResult() {
            return this.detectionResult;
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            if (image != null) {
                Rect clipRect = canvas.getClipBounds();
                int w = clipRect.width();
                int h = clipRect.height();
                int imgW = image.getWidth();
                int imgH = image.getHeight();

                float scaleX = ((float) w) / imgW;
                float scaleY = ((float) h) / imgH;
                float scale = scaleX;
                if (scaleX > scaleY)
                    scale = scaleY;
                imgW = (int) (scale * imgW);
                imgH = (int) (scale * imgH);
                Rect dst = new Rect();
                dst.left = (w - imgW) / 2;
                dst.top = (h - imgH) / 2;
                dst.right = dst.left + imgW;
                dst.bottom = dst.top + imgH;


                canvas.drawBitmap(image, null, dst, null);

                if (detectionResult != null) {
                    Paint myPaint = new Paint();
                    myPaint.setColor(Color.GREEN);
                    myPaint.setStyle(Paint.Style.STROKE);
                    myPaint.setStrokeWidth(2);
                    int x1 = (int) (detectionResult.getFaceRect().left * scale + dst.left);
                    int y1 = (int) (detectionResult.getFaceRect().top * scale + dst.top);
                    int x2 = (int) (detectionResult.getFaceRect().right * scale + dst.left);
                    int y2 = (int) (detectionResult.getFaceRect().bottom * scale + dst.top);

                    canvas.drawRect(x1, y1, x2, y2, myPaint);
                }
            }
        }
    }

    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                requestCameraPermission();
//            }
            switch (requestCode) {
                case 1:
                    // If request is cancelled, the result arrays are empty.
                    int i = 0;
                    boolean showRationaleStorage = false;
                    boolean showRationaleCamera = false;
                    boolean callStorage = false;
                    boolean callCamera = false;

                    for (String per : permissions) {
                        if (grantResults[i++] == PackageManager.PERMISSION_DENIED) {
                            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, per);
                            if (!showRationale) {
                                //user also CHECKED "never ask again"
                                if (per.equals(Manifest.permission.CAMERA)) {
                                    showRationaleCamera = true;
                                } else if (per.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showRationaleStorage = true;
                                }
                            } else if (per.equals(Manifest.permission.CAMERA)) {
                                callCamera = true;
                            } else if (per.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                callStorage = true;
                            }
                        }
                    }
                    if (showRationaleCamera && showRationaleStorage)
                        Toast.makeText(this, "You declined to allow the app to access Camera and Storage", Toast.LENGTH_SHORT).show();
                    else if (showRationaleCamera)
                        Toast.makeText(this, "You declined to allow the app to access Camera", Toast.LENGTH_SHORT).show();
                    else if (showRationaleStorage)
                        Toast.makeText(this, "You declined to allow the app to access Storage", Toast.LENGTH_SHORT).show();
                    else if (callStorage && callCamera)
                        Toast.makeText(this, "Please allow app to access Camera and Storage", Toast.LENGTH_SHORT).show();
                    else if (callCamera)
                        Toast.makeText(this, "Please allow app to access Camera", Toast.LENGTH_SHORT).show();
                    else if (callStorage)
                        Toast.makeText(this, "Please allow app to access Storage", Toast.LENGTH_SHORT).show();
                    else {
                        if (mCardScanner == null) {
                            mCardScanner = new RecogEngine();
                            mCardScanner.initEngine(MainActivity.this,MainActivity.this);
                        }
                        initEngine();//memory leak
                        return;
                    }
                    if (callCamera || callStorage) {
                        requestCameraPermission();
                    }
            }
        } else {
            requestCameraPermission();
        }
    }

}
