package com.accurascan.accuraemirates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.accurascan.accuraemirates.custom.CustomTextView;
import com.docrecog.scan.FileUtils;
import com.docrecog.scan.Util;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceLockHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.NumberFormat;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

public class FaceMatchActivity extends Activity implements FaceCallback {
    int ind;

    MyView image1;
    MyView image2;
    CustomTextView txtScore;
    boolean bImage2 = false;
    boolean bImage1 = false;

    float[] inputFeature;
    float[] matchFeature;

    final private int PICK_IMAGE = 1; // request code of select image from gallery
    final private int CAPTURE_IMAGE = 2; //request code of capture image in camera

    Bitmap face1, face2 = null; // used to contain image bitmap of front side image (face1) and  back side image (face2)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facematch);

        if (Util.isPermissionsGranted(this)) {
            init();
        } else {
            requestCameraPermission();
        }

       /* initEngine(); //initialize the FaceMatch Engine

        //handle click of gallery button of front side image that is used to select image from gallery
        findViewById(R.id.btnGallery1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isEmailSent = false;
                ind = 1;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of front side image  that is used to capture image
        findViewById(R.id.btnCamera1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isEmailSent = false;
                ind = 1;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File f = new File(FaceMatchActivity.this.getExternalFilesDir(null),"temp.jpg");

                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                Uri uriForFile = FileProvider.getUriForFile(
                        FaceMatchActivity.this,
                        "com.accurascan.demoapp.provider",
                        f
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });

        //handle click of gallery button of back side image
        findViewById(R.id.btnGallery2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isEmailSent = false;
                ind = 2;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of back side image
        findViewById(R.id.btnCamera2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isEmailSent = false;
                ind = 2;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File f = new File(FaceMatchActivity.this.getExternalFilesDir(null),"temp.jpg");

                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                Uri uriForFile = FileProvider.getUriForFile(
                        FaceMatchActivity.this,
                        "com.accurascan.demoapp.provider",
                        f
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });

        findViewById(R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtScore = (CustomTextView) findViewById(R.id.tvScore);
        txtScore.setText("Match Score : 0 %");

        image1 = new MyView(this);  //initialize the view of front image
        image2 = new MyView(this);  //initialize the view of back side image*/
    }

    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);

                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    init();
                }
                return;
            }
        }
    }

    private void init() {
        initEngine();
        //handle click of gallery button of front side image that is used to select image from gallery
        findViewById(R.id.btnGallery1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 1;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of front side image  that is used to capture image
        findViewById(R.id.btnCamera1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 1;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                Uri uriForFile = FileProvider.getUriForFile(
                        FaceMatchActivity.this,
                        getPackageName() + ".provider",
                        f
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });

        //handle click of gallery button of back side image
        findViewById(R.id.btnGallery2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 2;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
            }
        });

        //handle click of camera button of back side image
        findViewById(R.id.btnCamera2).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ind = 2;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                Uri uriForFile = FileProvider.getUriForFile(
                        FaceMatchActivity.this,
                        getPackageName() + ".provider",
                        f
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });

        findViewById(R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtScore = (CustomTextView) findViewById(R.id.tvScore);
        txtScore.setText("Match Score : 0 %");

        image1 = new MyView(this);  //initialize the view of front image
        image2 = new MyView(this);  //initialize the view of back side image
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == PICK_IMAGE) { //handle request code PICK_IMAGE used for selecting image from gallery

                if (data == null) // data contain result of selected image from gallery and other
                    return;
                if (data.getData() == null) // data contain result of selected image from gallery and other
                    return;
                //create a bitmap of selecetd image from gallery by using its real path
                Bitmap bmp = rotateImage(FileUtils.getPath(this, data.getData()));
//                Bitmap bmp = rotateImage(getRealPathFromUri( data.getData()));
                if (bmp != null) {
                    Bitmap nBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);

                    // ind is a integer variable used to handle front Image(Face 1) and back Image(Face 2) 1= for face1 and 2= for face 2
                    if (ind == 1) {
                        face1 = nBmp;
                        image1.setImage(nBmp);
                        SetImageView1();    //create ImageView of front image
                    } else if (ind == 2) {
                        face2 = nBmp;
                        image2.setImage(nBmp);
                        SetImageView2();    //create ImageView of back image
                    }

                    int w = nBmp.getWidth(); //getting width of selected image bitmap
                    int h = nBmp.getHeight(); //getting height of selected image bitmap

                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);

                    if (ind == 1)
                        // ind =1 so it detect front side imagee (face1)
                        FaceLockHelper.DetectLeftFace(buff.array(), w, h);
                    else {
                        if (image1.getFaceDetectionResult() != null) {
                            FaceLockHelper.DetectRightFace(buff.array(), w, h, image1.getFaceDetectionResult().getFeature());
                        } else {
                            // ind = 2 so it detect back side image (face2)
                            FaceLockHelper.DetectRightFace(buff.array(), w, h, null);
                        }
                    }
                } else {
                    Toast.makeText(this, "Choose Image Again", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAPTURE_IMAGE) { // handle request code CAPTURE_IMAGE used for capture image in camera
//                File f = FaceMatchActivity.this.getExternalFilesDir(null);

                File f = new File(Environment.getExternalStorageDirectory().toString());
                File ttt = null;

                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        ttt = temp;
                        break;
                    }
                }
                if (ttt == null)
                    return;

                //create a bitmap of captured image in camera
                Bitmap bmp = rotateImage(ttt.getAbsolutePath());
                ttt.delete();
                if (bmp != null) {
                    Bitmap nBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);

                    // ind is a integer variable used to handle front side image (Face 1) and bck side image (Face 2) 1= for face1 and 2= for face 2
                    if (ind == 1) {
                        face1 = nBmp;
                        image1.setImage(nBmp);
                        SetImageView1();      //create ImageView of front side image (face 1)
                    } else if (ind == 2) {
                        face2 = nBmp;
                        image2.setImage(nBmp);
                        SetImageView2();      //create ImageView of back side image (face 2)
                    }

                    int w = nBmp.getWidth(); //getting width of captured image bitmap
                    int h = nBmp.getHeight(); //getting height of captured image bitmap

                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);

                    if (ind == 1)
                        // ind =1 so it detect front side image
                        FaceLockHelper.DetectLeftFace(buff.array(), w, h);
                    else {

                        if (image1.getFaceDetectionResult() != null) {
                            FaceLockHelper.DetectRightFace(buff.array(), w, h, image1.getFaceDetectionResult().getFeature());
                        } else {
                            // ind = 2 so it detect back side image
                            FaceLockHelper.DetectRightFace(buff.array(), w, h, null);
                        }
                    }
                }
            }
        }
    }

    private void SetImageView1() {
        //create imageView for face 1
        if (!bImage1) {
            FrameLayout layout = (FrameLayout) findViewById(R.id.ivCardLayout);
            ImageView ivCard = (ImageView) findViewById(R.id.ivCard);
            image1.getLayoutParams().height = ivCard.getHeight();
            image1.requestLayout();
            layout.removeAllViews();
            layout.addView(image1);
            bImage1 = true;
        }
    }

    private void SetImageView2() {
        //create imageView for face 2
        if (!bImage2) {
            FrameLayout layout2 = (FrameLayout) findViewById(R.id.ivFaceLayout);
            ImageView ivFace = (ImageView) findViewById(R.id.ivFace);
            image2.getLayoutParams().height = ivFace.getHeight();
            image2.requestLayout();
            layout2.removeAllViews();
            layout2.addView(image2);
            bImage2 = true;
        }
    }

    private Uri getUri() {
        //get Uri of external storage
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public String getRealPathFromUri(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        if (cursor == null)
            return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // get original pth from uri
    public String getAbsolutePath(Uri uri) {
        if (Build.VERSION.SDK_INT >= 19) {
            String arr[] = uri.getLastPathSegment().split(":");
            String id;
            if (arr.length > 1)
                id = arr[1];
            else
                id = uri.getLastPathSegment();
            final String[] imageColumns = {MediaStore.Images.Media.DATA};
            final String imageOrderBy = null;
            Uri tempUri = getUri();
            Cursor imageCursor = getContentResolver().query(tempUri, imageColumns,
                    MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);
            if (imageCursor.moveToFirst()) {
                return imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } else {
                return null;
            }
        } else {
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else
                return null;
        }
    }

    // used for resizing the image bitmap
    // parameter to pass : Bitmap image, int maxSize
    // return resize bitmap

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

    // used to convert string path to Uri
    //parameter to pass : String path
    //return Uri
    private Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    //used for get bitmap image from given path
    //parameter to pass :String path
    //return bitmap

    private Bitmap decodeFileFromPath(String path) {

        Uri uri = getImageUri(path); // convert string path to Uri
        InputStream in = null;

        try {
            in = getContentResolver().openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            int inSampleSize = 2048;

            if (o.outHeight > inSampleSize || o.outWidth > inSampleSize) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(inSampleSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = getContentResolver().openInputStream(uri);
            int MAXCAP_SIZE = 512;

            Bitmap b = getResizedBitmap(BitmapFactory.decodeStream(in, null, o2), MAXCAP_SIZE);  //resizing the bitmap image
            in.close();

            return b;

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //handle File Not Found Exception
        } catch (IOException e) {
            e.printStackTrace(); // handle IO exception
        }
        return null;
    }

    //Used for rotate image as pe current orientation
    //parameter to pass : String path
    // return bitmap
    private Bitmap rotateImage(final String path) {
        if (path == null)
            return null;
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

    //Class use for creating a view
    public class MyView extends View {
        Bitmap image = null;
        FaceDetectionResult detectionResult = null;

        @SuppressLint("SdCardPath")
        public MyView(Context context) {
            super(context);

            @SuppressWarnings("deprecation")
            LayoutParams param = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

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

    //its method used for initEngine of face match
    private void initEngine() {
        //call Sdk  method InitEngine
        // parameter to pass : FaceCallback callback, int fmin, int fmax, float resizeRate, String modelpath, String weightpath, AssetManager assets
        // this method will return the integer value
        //  the return value by initEngine used the identify the particular error
        // -1 - No key found
        // -2 - Invalid Key
        // -3 - Invalid Platform
        // -4 - Invalid License

        writeFileToPrivateStorage(R.raw.model, "model.prototxt"); //write fie to private storage
        File modelFile = getApplicationContext().getFileStreamPath("model.prototxt");
        String pathModel = modelFile.getPath();
        writeFileToPrivateStorage(R.raw.weight, "weight.dat");    //write file to private storage
        File weightFile = getApplicationContext().getFileStreamPath("weight.dat");
        String pathWeight = weightFile.getPath();

        int nRet = FaceLockHelper.InitEngine(this, 30, 800, 1.18f, pathModel, pathWeight, this.getAssets());
        Log.i("facematch", "InitEngine: " + nRet);

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

    @Override
    public void onInitEngine(int ret) {
    }

    //call if face detect
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            image1.setImage(createFromARGB(faceResult.getNewImg(), faceResult.getNewWidth(), faceResult.getNewHeight()));
            image1.setFaceDetectionResult(faceResult);
            inputFeature = faceResult.getFeature().clone();
            Bitmap nBmp = image2.getImage();
            if (nBmp != null) {
                int w = nBmp.getWidth();
                int h = nBmp.getHeight();
                int s = (w * 32 + 31) / 32 * 4;
                ByteBuffer buff = ByteBuffer.allocate(s * h);
                nBmp.copyPixelsToBuffer(buff);
                if (image1.getFaceDetectionResult() != null) {
                    FaceLockHelper.DetectRightFace(buff.array(), w, h, image1.getFaceDetectionResult().getFeature());
                } else {
                    FaceLockHelper.DetectRightFace(buff.array(), w, h, null);
                }
            }
        } else {
            image1.setFaceDetectionResult(null);
            inputFeature = null;
            Bitmap nBmp = image2.getImage();
            if (nBmp != null) {
                int w = nBmp.getWidth();
                int h = nBmp.getHeight();
                int s = (w * 32 + 31) / 32 * 4;
                ByteBuffer buff = ByteBuffer.allocate(s * h);
                nBmp.copyPixelsToBuffer(buff);
                if (image1.getFaceDetectionResult() != null) {
                    FaceLockHelper.DetectRightFace(buff.array(), w, h, image1.getFaceDetectionResult().getFeature());
                } else {
                    FaceLockHelper.DetectRightFace(buff.array(), w, h, null);
                }
            }
        }
        calcMatch();
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            image2.setImage(createFromARGB(faceResult.getNewImg(), faceResult.getNewWidth(), faceResult.getNewHeight()));
            image2.setFaceDetectionResult(faceResult);
            matchFeature = faceResult.getFeature().clone();
        } else {
            image2.setFaceDetectionResult(null);
            matchFeature = null;
        }
        calcMatch();
    }

    @Override
    public void onExtractInit(int ret) {
    }

    //method for calulate the match score
    public void calcMatch() {
        if (image1.getFaceDetectionResult() == null || image2.getFaceDetectionResult() == null) {
            txtScore.setText("Match Score : 0 %");
        } else {
            float score = FaceLockHelper.Similarity(inputFeature, matchFeature, matchFeature.length);
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(1);
            String ss = nf.format(score * 100);
            txtScore.setText("Match Score : " + ss + " %");
        }
    }

    //method for write file in private storage
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
            e.printStackTrace(); // handle file not found exception
        } catch (IOException e) {
            e.printStackTrace();// handle IO exception
        }
    }

    public Bitmap createFromARGB(byte[] buffer, int width, int height) {
        Bitmap bmp;
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(buffer));
        return bmp;
    }
}
