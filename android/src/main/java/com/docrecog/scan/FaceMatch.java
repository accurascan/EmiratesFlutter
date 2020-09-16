package com.docrecog.scan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.accurascan.accuraemirates.R;
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

import io.flutter.plugin.common.MethodChannel;

public class FaceMatch {
    private final Context class_context;
    private MethodChannel.Result facematch_resutl;
    public static Bitmap face2;
    public static Bitmap face1;
    public static MyView image1;
    public static MyView image2;
    FaceDetectionResult leftResult = null;
    FaceDetectionResult rightResult = null;
    float match_score = 0.0f;
    private boolean faceResult_match = false;

    public FaceMatch(String imagepath, String face, MethodChannel.Result result, Context context) {
        byte[] decodedString = Base64.decode(face, Base64.DEFAULT);
        face1 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        class_context=context;

//        initEngine();

        image1 = new MyView(class_context);  //initialize the view of front image
        image2 = new MyView(class_context);

        File file = new File(imagepath);

        facematch_resutl = result;
        facematch(file);
    }


    public void facematch(File f) {
        String filename = f.getName();
        File myDir = new File(f.getParent());
        String[] children = myDir.list();
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(filename)) {
            } else {
                new File(myDir, children[i]).delete();
            }
        }

//        File ttt = null;
//        for (File temp : f.listFiles()) {
//            if (temp.getName().equals("temp.jpg")) {
//                ttt = temp;
//                break;
//            }
//        }
//        if (ttt == null)
//            return;
        Bitmap bmp = rotateImage(f.getPath());
//        f.delete();
        face2 = bmp.copy(Bitmap.Config.ARGB_8888, true);
//        ivUserProfile2.setImageBitmap(face2);
//        ivUserProfile2.setVisibility(View.VISIBLE);

        //if (RecogEngine.g_recogResult.faceBitmap != null) {
        if (face1 != null && !face1.isRecycled()) {
            //Bitmap nBmp = RecogEngine.g_recogResult.faceBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap nBmp = face1.copy(Bitmap.Config.ARGB_8888, true);
            image1.setImage(nBmp);
//             Bitmap temp_img=image1.getImage();
            int w = nBmp.getWidth();
            int h = nBmp.getHeight();
            int s = (w * 32 + 31) / 32 * 4;
            ByteBuffer buff = ByteBuffer.allocate(s * h);
            nBmp.copyPixelsToBuffer(buff);
            FaceLockHelper.DetectLeftFace(buff.array(), w, h);
        }
    }

    //Copy file to privateStorage
    public void writeFileToPrivateStorage(int fromFile, String toFile) {
        InputStream is = class_context.getApplicationContext().getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try {
            FileOutputStream fos = class_context.getApplicationContext().openFileOutput(toFile, Context.MODE_PRIVATE);

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

    private Bitmap rotateImage(final String path) {

        Bitmap b = decodeFileFromPath(path);

        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    break;
                default:
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    //b.copyPixelsFromBuffer(ByteBuffer.)
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return b;
    }

    private Bitmap decodeFileFromPath(String path) {
        Uri uri = getImageUri(path);
        InputStream in = null;
        try {
            in = class_context.getContentResolver().openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            int inSampleSize = 1024;
            if (o.outHeight > inSampleSize || o.outWidth > inSampleSize) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(inSampleSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = class_context.getContentResolver().openInputStream(uri);
            // Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            int MAXCAP_SIZE = 512;
            Bitmap b = getResizedBitmap(BitmapFactory.decodeStream(in, null, o2), MAXCAP_SIZE);
            in.close();

            return b;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    //Calculate Pan and Adhare facematch score
    public void calcMatch() {
        if (leftResult == null || rightResult == null) {
            match_score = 0.0f;
        } else {
            match_score = FaceLockHelper.Similarity(leftResult.getFeature(), rightResult.getFeature(), rightResult.getFeature().length);
            match_score *= 100.0f;
        }
        if (faceResult_match) {
            facematch_resutl.success(String.valueOf(match_score));
//            messageChannel.send(String.valueOf(match_score));
        }
//        tvFaceMatchScore1.setText(String.valueOf(match_score));
//        llFaceMatchScore.setVisibility(View.VISIBLE);
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
}
