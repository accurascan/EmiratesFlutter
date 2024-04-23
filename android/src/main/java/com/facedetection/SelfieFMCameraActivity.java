package com.facedetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.accurascan.accuraemirates.BuildConfig;
//import com.accurascan.accuraemirates.sdk.R;
import com.accurascan.accuraemirates.R;
import com.docrecog.scan.CameraActivity;
import com.facedetection.common.BitmapUtils;
import com.facedetection.common.CameraSource;
import com.facedetection.common.CameraSourcePreview;
import com.facedetection.common.FrameMetadata;
import com.facedetection.common.GraphicOverlay;
import com.facedetection.view.CircleOverlayView;
import com.facedetection.facedetectionutils.FaceDetectionProcessor;
import com.facedetection.facedetectionutils.FaceDetectionResultListener;
import com.facedetection.model.AccuraFMCameraModel;
import com.facedetection.utils.AccuraFaceMatchLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodChannel;

public final class SelfieFMCameraActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback {
    private static final String TAG = "FMSelfieCamera";
    private static final int PERMISSION_REQUESTS = 1;
    private static final int FACEMATCH_AUTO_CAPTURE_EXECUTE = 2;

    public static MethodChannel.Result faceImage;

    private CameraSource cameraSource = null;
    private GraphicOverlay fireFaceOverlay = null;
    private LinearLayout logoLL;
    private ImageView logoIV;
    private TextView poweredByIV;
    private CameraSourcePreview cameraPreview;
    private FaceDetectionProcessor processor;
    FaceDetectionResultListener faceDetectionResultListener = null;
    private TextView tv_status, tv_status1;
    private RelativeLayout centerFrameLayout, livenessFeedbackContainer;
    private ProgressDialog mProgressDialog;
    AccuraFMCameraModel verificationResult;
    FMCameraScreenCustomization FMCameraScreenCustomization;

    @Keep
    public static Intent getCustomIntent(Activity activity, FMCameraScreenCustomization FMCameraScreenCustomization) {
        Intent intent = new Intent(activity, SelfieFMCameraActivity.class);
        if (FMCameraScreenCustomization != null) {
            intent.putExtra("accura.fm.customization", FMCameraScreenCustomization);
        }
//        intent.putExtra("accura.liveness.url", liveness_url);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri.toString());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fm_selfie_camera);

        if (getIntent().hasExtra("accura.fm.customization"))
            FMCameraScreenCustomization = getIntent().getParcelableExtra("accura.fm.customization");
        if (FMCameraScreenCustomization == null) {
            FMCameraScreenCustomization = new FMCameraScreenCustomization();

            FMCameraScreenCustomization.backGroundColor = 0xFFC4C4C5;
            FMCameraScreenCustomization.closeIconColor = 0xFF000000;

            FMCameraScreenCustomization.feedbackBackGroundColor = Color.TRANSPARENT;
            FMCameraScreenCustomization.feedbackTextColor = Color.BLACK;
            FMCameraScreenCustomization.feedbackTextSize = 18;
        }

        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackframeMessage)) FMCameraScreenCustomization.feedBackframeMessage = getString(R.string.feedBackframeMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackAwayMessage)) FMCameraScreenCustomization.feedBackAwayMessage = getString(R.string.feedBackAwayMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackOpenEyesMessage)) FMCameraScreenCustomization.feedBackOpenEyesMessage = getString(R.string.feedBackOpenEyesMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackCloserMessage)) FMCameraScreenCustomization.feedBackCloserMessage = getString(R.string.feedBackCloserMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackCenterMessage)) FMCameraScreenCustomization.feedBackCenterMessage = getString(R.string.feedBackCenterMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackMultipleFaceMessage)) FMCameraScreenCustomization.feedBackMultipleFaceMessage = getString(R.string.feedBackMultipleFaceMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackHeadStraightMessage)) FMCameraScreenCustomization.feedBackHeadStraightMessage = getString(R.string.feedBackHeadStraightMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackLowLightMessage)) FMCameraScreenCustomization.feedBackLowLightMessage = getString(R.string.feedBackLowLightMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackBlurFaceMessage)) FMCameraScreenCustomization.feedBackBlurFaceMessage = getString(R.string.feedBackBlurFaceMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackGlareFaceMessage)) FMCameraScreenCustomization.feedBackGlareFaceMessage = getString(R.string.feedBackGlareFaceMessage);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedBackProcessingMessage)) FMCameraScreenCustomization.feedBackProcessingMessage = getString(R.string.processing);
        if (TextUtils.isEmpty(FMCameraScreenCustomization.feedbackDialogMessage)) FMCameraScreenCustomization.feedbackDialogMessage = getString(R.string.loading);
//        if (TextUtils.isEmpty(FMCameraScreenCustomization.rawdata)) FMCameraScreenCustomization.rawdata = getString(R.string.zero);

        FMCameraScreenCustomization.setLowLightTolerence(FMCameraScreenCustomization.lowLightTolerence);
        FMCameraScreenCustomization.setBlurPercentage(FMCameraScreenCustomization.blurPercentage);
        FMCameraScreenCustomization.setGlarePercentage(FMCameraScreenCustomization.glareMinPercentage, FMCameraScreenCustomization.glareMaxPercentage);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / BitmapUtils.WIDTH_RATIO);
        int height = (int) (width * BitmapUtils.HEIGHT_RATIO);
        centerFrameLayout = findViewById(R.id.centerFrameLayout);
        cameraPreview = findViewById(R.id.cameraPreview);
        tv_status = findViewById(R.id.tv_feedBack);
        tv_status1 = findViewById(R.id.tv_feedBack1);
        livenessFeedbackContainer = findViewById(R.id.livenessFeedbackContainer);
        ImageView oval_layout = findViewById(R.id.oval_layout);
        ImageView imClose = findViewById(R.id.im_close);

        imClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        logoLL = findViewById(R.id.logoLL);
        logoIV = findViewById(R.id.logoIV);
        poweredByIV = findViewById(R.id.poweredByIV);

        oval_layout.getLayoutParams().width = width;
        oval_layout.getLayoutParams().height = height;
        oval_layout.setBackground(getResources().getDrawable(R.drawable.fm_camera_overlay_frames));

        imClose.setColorFilter(FMCameraScreenCustomization.closeIconColor, android.graphics.PorterDuff.Mode.SRC_IN);

        tv_status.setText(TextUtils.isEmpty(FMCameraScreenCustomization.feedBackframeMessage) ? FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_FRAME_YOUR_FACE + "" : FMCameraScreenCustomization.feedBackframeMessage);
        tv_status.setTextColor(FMCameraScreenCustomization.feedbackTextColor);
        tv_status.setTextSize(FMCameraScreenCustomization.feedbackTextSize > 0 ? FMCameraScreenCustomization.feedbackTextSize : 18);
        tv_status1.setTextColor(FMCameraScreenCustomization.feedbackTextColor);

        if (FMCameraScreenCustomization.showlogo == 0) {
            logoLL.setVisibility(View.GONE);
        }
        if (FMCameraScreenCustomization.logoIcon != 0) {
            logoIV.setImageDrawable(getResources().getDrawable(FMCameraScreenCustomization.logoIcon));
            poweredByIV.setVisibility(View.GONE);
        }
        FeedBackShadow sp = new FeedBackShadow()
                .setShadowColor(0x77000000)
                .setShadowDy(dip2px(this, 0.5f))
                .setShadowRadius(dip2px(this, 3))
                .setShadowSide(FeedBackShadow.ALL);
        FeedBackView sd = new FeedBackView(sp, FMCameraScreenCustomization.feedbackBackGroundColor, 8, 8);
        ViewCompat.setBackground(livenessFeedbackContainer, sd);
        livenessFeedbackContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        CircleOverlayView circleOverlayView = new CircleOverlayView(this);
        circleOverlayView.setColor(FMCameraScreenCustomization.backGroundColor);
        circleOverlayView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        centerFrameLayout.removeAllViews();
        centerFrameLayout.addView(circleOverlayView);

        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
        initializeProgressDialog(FMCameraScreenCustomization.feedbackDialogMessage);
        verificationResult = new AccuraFMCameraModel();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, fireFaceOverlay);
            cameraSource.setFacing(FMCameraScreenCustomization.facing);
        }

        try {
            processor = new FaceDetectionProcessor();
            processor.setFaceDetectionResultListener(getFaceDetectionListener());
            processor.setCustomization(FMCameraScreenCustomization);
            processor.initEngine(this);
            cameraSource.setMachineLearningFrameProcessor(processor);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                AccuraFaceMatchLog.loge(TAG, "Can not create image processor: " + e.toString());
            }
        }

    }

    String status = "";

    private FaceDetectionResultListener getFaceDetectionListener() {
        if (faceDetectionResultListener == null)
            faceDetectionResultListener = new FaceDetectionResultListener() {
                private boolean isFirst = false;

                @Override
                public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {

                    if (!isFirst){ isFirst = true; fillFeedBackText(200);}
                    Uri uri = null;
                    if (originalCameraImage != null && !originalCameraImage.isRecycled()) {
                        try {
                            Rect rect = frameMetadata.getRect();
                            Bitmap bmCard = Bitmap.createBitmap(originalCameraImage, rect.left,rect.top,rect.width(),rect.height());
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bmCard.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            verificationResult.setFaceBiometrics(bos.toByteArray());
                            bmCard.recycle();

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
                            Uri myUri;
                            try {
                                myUri = Uri.fromFile(File.createTempFile(
                                        "IMG_",  // prefix
                                        ".jpg",         // suffix
                                        getCacheDir()      // directory
                                ));
                            } catch (IOException e) {
                                myUri = Uri.fromFile(new File(getCacheDir(), "IMG_" + timeStamp + ".jpg"));
                            }
                            uri = checkImage(verificationResult.getArray(), myUri);
                        } catch (Exception e) {
                        }
                    }
                    if (uri != null && uri.getPath() != null) {
                        status = FMCameraScreenCustomization.feedBackProcessingMessage;
//                        runOnUiThread(() -> showProgressDialog());
                        mHandler.sendEmptyMessageDelayed(FACEMATCH_AUTO_CAPTURE_EXECUTE, 2000);

                    } else {
                        mHandler.sendEmptyMessageDelayed(0, 2000);
                    }
                    String base64I = bitmapToBase64(originalCameraImage);
                    runOnUiThread(() -> CameraActivity.facematch_resutl.success(String.valueOf(base64I)));
                    finish();
                    originalCameraImage.recycle();
                }

                public String bitmapToBase64(Bitmap bitmap) {
                    if (bitmap == null) {
                        return null;
                    }

                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        return Base64.encodeToString(byteArray, Base64.DEFAULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void onFeedBackMessage(int s) {
                    if (status.equals(FMCameraScreenCustomization.feedBackProcessingMessage)) {
                        return;
                    }
                    switch (s) {
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_APPROVED:
                            status = FMCameraScreenCustomization.feedBackProcessingMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_CENTER:
                            status = FMCameraScreenCustomization.feedBackCenterMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_AWAY:
                            status = FMCameraScreenCustomization.feedBackAwayMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_CLOSER:
                            status = FMCameraScreenCustomization.feedBackCloserMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_OPEN_EYES:
                            status = FMCameraScreenCustomization.feedBackOpenEyesMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_FRAME_YOUR_FACE:
                            status = FMCameraScreenCustomization.feedBackframeMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_MULTIPLE_FACES:
                            status = FMCameraScreenCustomization.feedBackMultipleFaceMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_HEAD_STEADY:
                            status = FMCameraScreenCustomization.feedBackHeadStraightMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_DARK_FACE:
                            status = FMCameraScreenCustomization.feedBackLowLightMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_BLUR_FACE:
                            status = FMCameraScreenCustomization.feedBackBlurFaceMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_GLARE:
                            status = FMCameraScreenCustomization.feedBackGlareFaceMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_INVALID:
                            status = "Invalid License";
                            break;
                    }
                    if (!isFirst) {
                        isFirst = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fillFeedBackText(300);
                            }
                        });
                    }
                }

                private void fillFeedBackText(final int timeToBlink) {

                    final Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{Thread.sleep(timeToBlink);}catch (Exception e) {}
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(tv_status.getVisibility() == View.VISIBLE) {
                                        tv_status.setVisibility(View.INVISIBLE);
                                        if (!isDestroyed()) fillFeedBackText(400);
                                    } else {
                                        tv_status.setText(status);
                                        tv_status.setVisibility(View.VISIBLE);
                                        if (!status.equals(FMCameraScreenCustomization.feedBackProcessingMessage)) {
                                            if (!isDestroyed()) fillFeedBackText(600);
                                        }
                                    }
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onFailure(@NonNull Exception e) {

                }
            };

        return faceDetectionResultListener;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
            if (msg.what == 0) {
                if (processor != null)
                    processor.setTakePicture(true);
            } else if (msg.what == 1) {
//                tv_status.setText(TextUtils.isEmpty(FMCameraScreenCustomization.feedBackCenterMessage) ? FaceDetectionProcessor.ACCURA_LIVENESS_FEEDBACK_CODE_MOVE_PHONE_CENTER : FMCameraScreenCustomization.feedBackCenterMessage);
            } else if (msg.what == FACEMATCH_AUTO_CAPTURE_EXECUTE) {
                verificationResult.setStatus("1");
                dismissProgressDialog();
                Intent intent = new Intent();
                verificationResult.setFaceBiometrics(null);
                intent.putExtra("Accura.fm", verificationResult);
                setResult(RESULT_OK, intent);
                SelfieFMCameraActivity.this.finish();
            }
//                }
//            }, 3000);

        }
    };

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (cameraPreview == null) {
                    AccuraFaceMatchLog.loge(TAG, "Preview is null");
                }
                cameraPreview.start(cameraSource, fireFaceOverlay);
            } catch (IOException e) {
//                Logger.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private Uri checkImage(byte[] bitmapData, Uri uri) {

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(uri.getPath());
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (Exception e) {
//            AccuraFaceMatchLog.loge(TAG, "cI: \n" + Log.getStackTraceString(e));
            return null;
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
//                AccuraFaceMatchLog.loge(TAG, "fr-" + Log.getStackTraceString(e));
            }
            scanFile(uri);
            verificationResult.setFaceBiometric(uri);
        }
        return uri;
    }

    private void scanFile(Uri imageUri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        sendBroadcast(scanIntent);
    }

    private void initializeProgressDialog(String progressMessage) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(progressMessage);
    }

    public void showProgressDialog() {
        try {
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dip2px(Context context, float dpValue) {
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Throwable throwable) {
            // igonre
        }
        return 0;
    }

    private String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NonNull int[] grantResults) {
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
