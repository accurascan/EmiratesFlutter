package com.docrecog.scan;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.CameraProfile;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accurascan.accuraemirates.FocusManager;
import com.accurascan.accuraemirates.FocusManager.Listener;
import com.accurascan.accuraemirates.R;
import com.accurascan.accuraemirates.camera.CameraHolder;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceLockHelper;
import com.model.OcrData;
import com.motiondetection.data.GlobalData;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.common.StringCodec;
import io.flutter.plugin.platform.PlatformView;

import static org.opencv.BuildConfig.DEBUG;

public class CameraActivity extends SensorsActivity implements PlatformView, MethodChannel.MethodCallHandler,
        SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PreviewCallback,
        Camera.PictureCallback, Listener, OnTouchListener, View.OnClickListener, OCRCallback {

    private static final String CHANNEL = "scan_preview";
    private int width;
    private int height;
    private Context context;
    private PluginRegistry.Registrar registrar;
    private Activity activity;
    private MethodChannel channel;
    private BasicMessageChannel messageChannel;
    private BasicMessageChannel messageChannel_facematch;
    private int cameraid;
    private MethodChannel methodChannel;
    private FlutterEngine class_flutterEngine;


    protected static final int IDLE = 0; // preview is active
    protected static final int SAVING_PICTURES = 5;
    private static final int PREVIEW_STOPPED = 0;
    // Focus is in progress. The exact focus state is in Focus.java.
    private static final int FOCUSING = 2;
    private static final int SNAPSHOT_IN_PROGRESS = 3;
    private static final int SELFTIMER_COUNTING = 4;
    private static final String TAG = "CameraActivity";
    private static final int FIRST_TIME_INIT = 0;
    private static final int CLEAR_SCREEN_DELAY = 1;
    private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 4;
    // number clear
    private static final int TRIGER_RESTART_RECOG = 5;
    private static final int TRIGER_RESTART_RECOG_DELAY = 40; //30 ms
    // The subset of parameters we need to update in setCameraParameters().
    private static final int UPDATE_PARAM_INITIALIZE = 1;
    private static final int UPDATE_PARAM_PREFERENCE = 4;
    private static final int UPDATE_PARAM_ALL = -1;
    private static final long VIBRATE_DURATION = 200L;
    private static boolean LOGV = false;
    private static RecogEngine mCardScanner;
    private static int mRecCnt = 0; //counter for mrz detecting
    private Context mContext = null;
    private TextView mScanTitle = null;
    //    private TextView mScanMsg = null;
    private ImageView mFlipImage = null;
    private final Lock _mutex = new ReentrantLock(true);
    List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

    public OcrData application;
    private RecogResult g_recogResult;
    private File fileCacheDirectory;


//    RecogEngine application = new RecogEngine().getInstance();

    /////////////////////
    //audio
    MediaPlayer mediaPlayer = null;
    AudioManager audioManager = null;

    final Handler mHandler = new Handler();
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    protected Camera mCameraDevice;
    // The first rear facing camera
    Parameters mParameters;
    SurfaceHolder mSurfaceHolder;
    // This handles everything about focus.
    FocusManager mFocusManager;
    int mPreviewWidth = 1280;//640;
    int mPreviewHeight = 720;//480;
    /*private CheckBox chkRecogType;*/
    private byte[] cameraData;
    private Parameters mInitialParams;
    private int mCameraState = PREVIEW_STOPPED;
    private int mCameraId;
    private boolean mOpenCameraFail = false;
    private boolean mCameraDisabled = false, isTouchCalled;
    Thread mCameraOpenThread = new Thread(new Runnable() {
        public void run() {
            try {
//                mCameraDevice = Util.openCamera(getActivity(), mCameraId);
                mCameraDevice = Camera.open();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
//                    }
//                }
            } catch (Exception e) {
//                mOpenCameraFail = true;
//                mCameraDisabled = true;
            }
        }
    });
    private boolean mOnResumePending;
    private boolean mPausing;
    private boolean mFirstTimeInitialized;
    // When setCameraParametersWhenIdle() is called, we accumulate the subsets
    // needed to be updated in mUpdateSet.
    private int mUpdateSet;
    private View mPreviewFrame;
    RelativeLayout rel_main;// Preview frame area for SurfaceView.
    /*private TextView mModeView, mPreviewSizeView, mPictureSizeView;*/
    private boolean mbVibrate;
    private Dialog dialog;
    // The display rotation in degrees. This is only valid when mCameraState is
    // not PREVIEW_STOPPED.
    private int mDisplayRotation;
    // The value for android.hardware.Camera.setDisplayOrientation.
    private int mDisplayOrientation;
    private boolean mIsAutoFocusCallback = false;
    Thread mCameraPreviewThread = new Thread(new Runnable() {
        public void run() {
            initializeCapabilities();
            startPreview();
        }
    });

    private String cardType = "Emirates National ID";
    private String countryname = "United Arab Emirates";

    private OpenCvHelper openCvHelper;
    private boolean isbothavailable = true;
    private boolean isfront = true;
    private boolean isback = false;
    private String cardside = "Front";
    private int cardpos = 0;
    private int checkmrz = 0; //0 ideal 1 not require 2 require
    private int gotmrz = -1;
    private boolean isDone = false;
    private boolean isBackPressed = false;

    View rel_left, rel_right;
    int imgheight;
    private int rectW, rectH;
    DisplayMetrics dm;
    private int titleBarHeight = 0;
    private boolean doblurcheck = false;
    private boolean isBlurSet = false;
    private int ans;
    private String[][] Frontdata;
    private String[][] Backdata;
    private String framemessage;

    //    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Bitmap face1;
    public static MethodChannel.Result facematch_resutl;
    public static MethodChannel.Result facematch_resutl_match;
    public static FaceDetectionResult leftResult = null;
    public static FaceDetectionResult rightResult = null;
    float match_score = 0.0f;
    Size previewSize = null;
    private SurfaceView surfaceView;
    private boolean isPreviewSet = false;
    private int frameCount = 0;
    private String newMessage="";

    CameraActivity() {
        this.context = null;
        this.registrar = null;
        this.activity = null;
        this.cameraid = 0;
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        mContext = this;
        class_flutterEngine = flutterEngine;
        methodChannel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
        methodChannel.setMethodCallHandler(this);
    }

    public CameraActivity(Context context, PluginRegistry.Registrar mPluginRegistrar, int id) {
        this.context = context;
        this.registrar = mPluginRegistrar;
        this.activity = mPluginRegistrar.activity();
        this.cameraid = 0;

        channel = new MethodChannel(mPluginRegistrar.messenger(), "scan_preview");
        messageChannel = new BasicMessageChannel<>(mPluginRegistrar.messenger(), "scan_preview_message", StandardMessageCodec.INSTANCE);
        messageChannel_facematch = new BasicMessageChannel<>(mPluginRegistrar.messenger(), "scan_preview_message", StringCodec.INSTANCE);
        channel.setMethodCallHandler(this);

        dm = context.getResources().getDisplayMetrics();
        mCameraId = CameraHolder.instance().getBackCameraId();
        //String str = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        String[] defaultFocusModes = {"continuous-video", "auto", "continuous-picture"};
        mFocusManager = new FocusManager(defaultFocusModes);
        /*
         * To reduce startup time, we start the camera open and preview threads.
         * We make sure the preview is started at the end of onCreate.
         */
        mCameraOpenThread.start();

        mCameraPreview = new CameraPreview(this.context, mCameraDevice);
        mCameraPreview.setOnTouchListener(CameraActivity.this);

        SurfaceHolder holder = mCameraPreview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (mCardScanner == null) {
            mCardScanner = new RecogEngine();
            mCardScanner.initEngine(context, this);
            /**
             * Update filter value according to requirement
             * {@link RecogEngine#setFilter()}
             */
            mCardScanner.setFilter();
        }

        dm = context.getResources().getDisplayMetrics();
        ///audio init
        mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);

        openCvHelper = new OpenCvHelper(CameraActivity.this);

        application = new OcrData();
//        fileCacheDirectory = new File(getCacheDir(), "files");
//        if (!fileCacheDirectory.exists()) {
//            fileCacheDirectory.mkdir();
//        }

        isPreviewSet = false;
        // make sure preview filed is public to access in whole activity
//        preview = (SurfaceView) findViewById(R.id.camera_preview);

        try {
            Resources myResources = context.getResources();
            int idStatusBarHeight = myResources.getIdentifier("status_bar_height", "dimen", "android");
            if (idStatusBarHeight > 0) {
                titleBarHeight = context.getResources().getDimensionPixelSize(idStatusBarHeight);
            } else {
                titleBarHeight = 0;
            }
        } catch (
                Resources.NotFoundException e) {
            titleBarHeight = 0;
            e.printStackTrace();
        }

        if (LOGV) Log.i("a", "StatusBar Height= " + titleBarHeight);

        RecogEngine.setCardname(cardType);
        RecogEngine.setCountryname(countryname);

        SetTempleteImage();

        // Make sure camera device is opened.
        try {
            mCameraOpenThread.join();
//            mCameraOpenThread = null;
            if (mOpenCameraFail) {
                Util.showErrorAndFinish(activity, R.string.cannot_connect_camera);
                return;
            } else if (mCameraDisabled) {
                Util.showErrorAndFinish(activity, R.string.camera_disabled);
                return;
            }
        } catch (
                InterruptedException ex) {
            // ignore
        }

        mCameraPreviewThread.start();

        // do init
        // initializeZoomMax(mInitialParams);

        // Make sure preview is started.
        try {
            mCameraPreviewThread.join();
        } catch (
                InterruptedException ex) {
            // ignore
        }

//        mCameraPreviewThread = null;

//        mRecCnt = 0;
//        gotmrz = -1;
        g_recogResult = new RecogResult();
//            mScanTitle.setText("Scan Front Side of " + RecogEngine.getCardname());
        SetFrontTemplete();
        isDone = false;
        RecogEngine.setFrontData(null);
        RecogEngine.setBackData(null);
        face1 = null;
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (LOGV) Log.v(TAG, "onWindowFocusChanged.hasFocus=" + hasFocus
                + ".mOnResumePending=" + mOnResumePending);
        if (hasFocus && mOnResumePending) {
            doOnResume();
            mOnResumePending = false;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();

//        mbVibrate = true;
//        if (LOGV) Log.v(TAG, "onResume. hasWindowFocus()=" + hasWindowFocus());
//        if (mCameraDevice == null) {// && isKeyguardLocked()) {
//            if (LOGV) Log.v(TAG, "onResume. mOnResumePending=true");
//            if (hasWindowFocus()) {
//                doOnResume();
//            } else
//                mOnResumePending = true;
//        } else {
//            if (LOGV) Log.v(TAG, "onResume. mOnResumePending=false");
//            int currentSDKVersion = Build.VERSION.SDK_INT;
//
//            doOnResume();
//
//
//            mOnResumePending = false;
//        }
    }

    protected void doOnResume() {
        if (mOpenCameraFail || mCameraDisabled)
            return;

        // if (mRecogService != null && mRecogService.isProcessing())
        // showProgress(null);
        mRecCnt = 0;
        mPausing = false;

        // Start the preview if it is not started.
        if (mCameraState == PREVIEW_STOPPED) {
            try {
//                mCameraDevice = Util.openCamera(getActivity(), mCameraId);
                mCameraDevice = Camera.open();
                initializeCapabilities();
                startPreview();
            } catch (Exception e) {
                Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
                return;
            }
        }

        if (mSurfaceHolder != null) {
            // If first time initialization is not finished, put it in the
            // message queue.
            if (!mFirstTimeInitialized) {
                mHandler.sendEmptyMessage(FIRST_TIME_INIT);
            } else {
                initializeSecondTime();
            }
        }

        keepScreenOnAwhile();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.ivBack) {
            onBackPressed();
        }
    }

    // Snapshots can only be taken after this is called. It should be called
    // once only. We could have done these things in onCreate() but we want to
    // make preview screen appear as soon as possible.
    private void initializeFirstTime() {
        if (mFirstTimeInitialized)
            return;

//		mOrientationListener = new MyOrientationEventListener(this);
//		mOrientationListener.enable();

        mCameraId = CameraHolder.instance().getBackCameraId();

//        Util.initializeScreenBrightness(getWindow(), getContentResolver());
        mFirstTimeInitialized = true;
    }

    // If the activity is paused and resumed, this method will be called in
    // onResume.
    private void initializeSecondTime() {
        //mOrientationListener.enable();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        keepScreenOnAwhile();
    }

    private void resetScreenOn() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public View getView() {
        return mCameraPreview;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "scan#startCamera":
                break;
            case "facecrop":
                facematch_resutl = result;
                String imagepath = call.argument("cameraimage");
                String face = call.argument("cardfaceimage");
                new FaceMatch(imagepath, face, result, context);
                break;
            case "facematch":
                facematch_resutl = result;
                calcMatch();
                break;
        }
    }

    //Calculate Pan and Adhare facematch score
    public void calcMatch() {
        if (leftResult == null || rightResult == null) {
            match_score = 0.0f;
        } else {
            match_score = FaceLockHelper.Similarity(leftResult.getFeature(), rightResult.getFeature(), rightResult.getFeature().length);
            match_score *= 100.0f;
        }
        CameraActivity.facematch_resutl.success(String.format("%.2f", match_score));
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        // TODO Auto-generated method stub
//		Log.e(TAG, "onPreviewFrame mPausing=" + mPausing + ", mCameraState=" + mCameraState);

        if (mPausing) {
//			mCardScanner.isRecognizing = false;
            return;
        }

        if (mCameraState != IDLE) {
            mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
            return;
        } else {
//			mCardScanner.isRecognizing = true;
        }
        // generate jpeg image.
        final int width = camera.getParameters().getPreviewSize().width;
        final int height = camera.getParameters().getPreviewSize().height;
        final int format = camera.getParameters().getPreviewFormat();
        if (!GlobalData.isPhoneInMotion()) {
            Thread recogThread = new Thread(new Runnable() {
                int ret;
                ImageOpencv frameData;
                int faceret = 0; // detecting face return value
                Bitmap bmCard = null;

                //
                @Override
                public void run() {

                    if (bmCard != null && !bmCard.isRecycled()) {
                        bmCard.recycle();
                    }
                    int i = openCvHelper.doCheckFrame(data, width, height);
                    if (i > 0) {
                        // end to checking from native code
//                        showToast("");
                        YuvImage temp = new YuvImage(data, format, width, height, null);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        temp.compressToJpeg(new Rect(0, 0, temp.getWidth(), temp.getHeight()), 100, os);
                        temp = null;
                        //getting original bitmap of scan result
                        Bitmap bmp1 = BitmapFactory.decodeByteArray(os.toByteArray(), 0, os.toByteArray().length);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(mDisplayOrientation);

                        bmp1 = Bitmap.createBitmap(bmp1, 0, 0, bmp1.getWidth(), bmp1.getHeight(), matrix, true);
                        matrix.reset();

                        int totalHeight = dm.heightPixels - titleBarHeight;
                        Point centerOfCanvas = new Point(dm.widthPixels / 2, totalHeight / 2);
                        int left = centerOfCanvas.x - (rectW / 2);
                        int top = centerOfCanvas.y - (rectH / 2);
                        int right = centerOfCanvas.x + (rectW / 2);
                        int bottom = centerOfCanvas.y + (rectH / 2);
                        Rect frameRect = new Rect(left, top, right, bottom);

                        float widthScaleFactor = (float) dm.widthPixels / (float) height;
                        float heightScaleFactor = (float) (totalHeight) / (float) width;
                        frameRect.left = (int) (frameRect.left / widthScaleFactor);
                        frameRect.top = (int) (frameRect.top / heightScaleFactor);
                        frameRect.right = (int) (frameRect.right / widthScaleFactor);
                        frameRect.bottom = (int) (frameRect.bottom / heightScaleFactor);
                        Rect finalrect = new Rect((int) (frameRect.left), (int) (frameRect.top), (int) (frameRect.right), (int) (frameRect.bottom));

                        try {
                            if (finalrect.left >= 0 && finalrect.top >= 0 && finalrect.width() > 0 && finalrect.height() > 0) {
                                bmCard = Bitmap.createBitmap(bmp1, finalrect.left, finalrect.top, finalrect.width(), finalrect.height(), matrix, true);
//                        bmCard = Bitmap.createBitmap(bmp1, finalrect.left, finalrect.top, finalrect.width(), finalrect.height());//memory leak
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            bmCard = null;
                        }

                        bmp1.recycle();

                        _mutex.lock();
                        if (bmCard != null) {
                            if (!isfront) {
                                if (mRecCnt > 3) {
                                    frameData = openCvHelper.nativeCheckCardIsInFrame(CameraActivity.this, bmCard, doblurcheck);

                                    if (frameData != null && frameData.accuraOCR.equals(OpenCvHelper.AccuraOCR.SUCCESS)) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                list = new ArrayList<>();
//                                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
//                                                prodHashMap.put("message", "Processing...");
//                                                list.add(prodHashMap);
//                                                messageChannel.send(list);

                                                onUpdateProcess("3");
//                                        showToast("Processing...");
                                            }
                                        });
                                        Bitmap docBmp = bmCard.copy(Bitmap.Config.ARGB_8888, false);
                                        int width = docBmp.getWidth();
                                        int height = docBmp.getHeight();
                                        float ratio = width / height;

//                                Log.v("Pictures", "Width and height are " + width + "--" + height);

                                        if (width > height) {
                                            // landscape
                                            if (ratio > 1.8 || ratio < 1.6) {
                                                height = (int) (width / 1.69);
                                            }
                                        }

                                        docBmp = Bitmap.createScaledBitmap(docBmp, width, height, false);
                                        ret = mCardScanner.doRunData(docBmp, 0, g_recogResult);

                                        if (ret > 0) {
                                            gotmrz = 0;
                                        }
                                    }
                                }
                                mRecCnt++;
                            } else {
                                // bright front image for make face more bright
                                Bitmap bitmap = brightImage(bmCard);
                                frameData = openCvHelper.nativeCheckCardIsInFrame(CameraActivity.this, bitmap, doblurcheck);
//                            frameData = openCvHelper.nativeCheckCardIsInFrame(CameraActivity.this, bmCard, doblurcheck);
                            }
                        }

                        _mutex.unlock();

                        if (bmCard != null) {
                            CameraActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isDone) {
                                        if (!isfront && gotmrz == 0 && (frameData.finalData == null || !frameData.accuraOCR.equals(OpenCvHelper.AccuraOCR.SUCCESS))) {
                                            Bitmap docBmp = bmCard;
                                            if (AfterMapping(RecogEngine.getBackData()/*mrzData*/, docBmp.copy(Bitmap.Config.ARGB_8888, false))) {
                                                try {
                                                    docBmp.recycle();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                showResultActivity();
                                            } else {
                                                Log.d(TAG, "failed");
                                                list = new ArrayList<>();
                                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                                prodHashMap.put("message", framemessage);
                                                list.add(prodHashMap);
//                                                messageChannel.send(list);

                                                try {
                                                    docBmp.recycle();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (!mPausing && mCameraDevice != null) {
                                                    mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
                                                }

                                                mHandler.sendMessageDelayed(
                                                        mHandler.obtainMessage(TRIGER_RESTART_RECOG),
                                                        TRIGER_RESTART_RECOG_DELAY);
                                            }
                                        }
                                        if (frameData != null && frameData.accuraOCR.equals(OpenCvHelper.AccuraOCR.SUCCESS)) {
                                            Bitmap docBmp = bmCard;
                                            if (!frameData.rect.isEmpty() && frameData.rect.contains(",")) {
                                                docBmp = frameData.getBitmap();
                                                if (docBmp == null) {
                                                    String[] reststring = frameData.rect.split(",");
                                                    if (reststring.length == 4) {
                                                        Rect rect = new Rect(Integer.valueOf(reststring[0]), Integer.valueOf(reststring[1]), Integer.valueOf(reststring[2]), Integer.valueOf(reststring[3]));
                                                        docBmp = Bitmap.createBitmap(docBmp, rect.left, rect.top, rect.width(), rect.height());//memory leak
                                                    }
                                                }
                                            }
                                            if (AfterMapping(frameData.finalData, docBmp.copy(Bitmap.Config.ARGB_8888, false))) {
                                                try {
                                                    docBmp.recycle();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                showResultActivity();
                                            } else {
                                                Log.d(TAG, "failed");

                                                try {
                                                    docBmp.recycle();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                if (!mPausing && mCameraDevice != null) {
                                                    mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
                                                }

                                                mHandler.sendMessageDelayed(
                                                        mHandler.obtainMessage(TRIGER_RESTART_RECOG),
                                                        TRIGER_RESTART_RECOG_DELAY);
                                            }
                                        } else {
                                            Log.d(TAG, "failed");
                                            list = new ArrayList<>();
                                            try {
                                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                                prodHashMap.put("message", framemessage);
                                                list.add(prodHashMap);
//                                                messageChannel.send(list);
                                            } catch (IllegalStateException i) {

                                            }

                                            if (isDone) {
                                                return;
                                            }

                                            try {
                                                bmCard.recycle();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            if (!mPausing && mCameraDevice != null) {
                                                mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
                                            }

                                            mHandler.sendMessageDelayed(
                                                    mHandler.obtainMessage(TRIGER_RESTART_RECOG),
                                                    TRIGER_RESTART_RECOG_DELAY);
                                        }
                                    } else {
                                        bmCard.recycle();
                                    }
                                }

                            });
                        } else if (!mPausing && mCameraDevice != null) {
                            mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
                        }
                    } else if (i == 0) {
                        if (frameCount % 2 == 0) {
//                            CameraActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
////                            showToast("Keep Document Steady");
//                                    HashMap<String, String> prodHashMap = new HashMap<String, String>();
//                                    prodHashMap.put("message", "Keep Document Steady");
//                                    list.add(prodHashMap);
//                                    messageChannel.send(list);
//                                }
//                            });
                        }
                        if (!mPausing && mCameraDevice != null) {
                            mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
                        }

                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(TRIGER_RESTART_RECOG),
                                TRIGER_RESTART_RECOG_DELAY);
                    }

                    frameCount++;
                }

                private boolean AfterMapping(String[][] result, Bitmap image) {
                    if (result == null || isDone) {
                        return false;
                    }
                    if (isfront) {
                        if (RecogEngine.getFrontData() == null) {
                            RecogEngine.setFrontData(result);
                            RecogEngine.setFrontimage(image);
                        }
                        if (RecogEngine.getBackData() == null) {
                            flipImage();
                            SetBackTemplete();
                            list = new ArrayList<>();
                            HashMap<String, String> prodHashMap = new HashMap<String, String>();
                            prodHashMap.put("front", "done");
                            list.add(prodHashMap);
                            messageChannel.send(list);

                        }
                    } else if (isback) {
//                    if (RecogEngine.getBackData() == null) {
                        RecogEngine.setBackData(result);
                        RecogEngine.setBackimage(image);

                        if (RecogEngine.getFrontData() == null) {

                            flipImage();
                            //set back image templete
                            SetFrontTemplete();

                        }
                    }

                    if (RecogEngine.getFrontData() != null && RecogEngine.getBackData() != null && (gotmrz == 0 || !g_recogResult.lines.equals(""))) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            if (!isDone) {
                recogThread.start();
            }
        } else {
            if (!mPausing && mCameraDevice != null) {
                mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
            }

            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(TRIGER_RESTART_RECOG),
                    TRIGER_RESTART_RECOG_DELAY);
            frameCount++;
        }
//		}
    }

    private Bitmap brightImage(Bitmap bmCard) {
        Mat srcMat = new Mat();// src frame Mat
        Mat outMat = new Mat(); // bright image mat
        float invGamma = 1.0f / 1.5f;
        // convert bitmap to mat
        Utils.bitmapToMat(bmCard, srcMat);

        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total() * lookUpTable.channels())];
        for (int k = 0; k < lookUpTable.cols(); k++) {
            lookUpTableData[k] = saturate(Math.pow(k / 255.0, invGamma) * 255.0);
        }
        lookUpTable.put(0, 0, lookUpTableData);
        Core.LUT(srcMat, lookUpTable, outMat);
        // convert opencv Mat to bitmap
        Bitmap bmp = Bitmap.createBitmap(outMat.width(), outMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outMat, bmp);
        return bmp;
    }

    private byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    private final class getDocData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            list = new ArrayList<>();

            Frontdata = RecogEngine.getFrontData();
            Backdata = RecogEngine.getBackData();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (Frontdata != null) {
                final Bitmap frontBitmap = RecogEngine.getFrontimage();
                for (int i = 0; i < Frontdata.length; i++) {

//                TableRow tr =  new TableRow(this);
                    final String[] array = Frontdata[i];

                    if (array != null && array.length != 0) {
                        final String key = array[0];
                        Log.e(TAG, "run: key" + key);
                        //                key.replace("img11", "");
                        String value = array[1];

                        if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {

                            if (!key.contains("_img")) {

//                                    String _img = value;
//                                    HashMap<String, String> prodHashMap = new HashMap<String, String>();
//                                    prodHashMap.put("_img", _img);
//                                    list.add(prodHashMap);
                            } else {
                                try {
                                    if (key.toLowerCase().contains("face")) {
                                        try {
                                            if (face1 == null) {
                                                face1 = ImageOpencv.getImage(value);

                                            }
                                            if (DEBUG) { // TODO remove it
                                                if (face1 == null) {
                                                    face1 = g_recogResult.faceBitmap; //memory leak

                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    face1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                    byte[] b = baos.toByteArray();
                                                    value = Base64.encodeToString(b, Base64.DEFAULT);
                                                }
                                            }
                                            HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                            prodHashMap.put("face", value);
                                            list.add(prodHashMap);//memory leak

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
//                                            tv_value.setVisibility(View.GONE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }


                    }
                    if (frontBitmap != null && !frontBitmap.isRecycled()) {
                        try {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            frontBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                            String frontBitmapencoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);


                            HashMap<String, String> prodHashMap = new HashMap<String, String>();
                            prodHashMap.put("frontBitmap", frontBitmapencoded);
                            list.add(prodHashMap);

                            Bitmap iv_frontside = frontBitmap;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                    ly_front.setVisibility(View.GONE);

                    }
                });
            }

            if (Backdata != null) {
                final Bitmap BackImage = RecogEngine.getBackimage();

                for (int i = 0; i < Backdata.length; i++) {

                    String[] array = Backdata[i];

                    if (array != null && array.length != 0) {
                        String key = array[0];

                        String value = array[1];
                        if (!key.equalsIgnoreCase("mrz") && !key.toLowerCase().contains(".png")) {
                            if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {

                                if (!key.contains("_img")) {

                                    String _img = value;

                                    HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                    prodHashMap.put("_img", _img);
                                    list.add(prodHashMap);

                                } else {
                                    try {
                                        Bitmap bitmap = ImageOpencv.getImage(value);
                                        HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                        prodHashMap.put("_img", value);
                                        list.add(prodHashMap);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
//                                    tv_key.setText(key.replace("_img", "") + ":");
                            }
                        } else if (key.toLowerCase().contains("mrz")) {

                            int ret = 0;
                            int faceret = 0; // detecting face return value
                            Bitmap bmCard;
                            int mRecCnt = 0;
                            int mDisplayRotation = 0;
                            if (!TextUtils.isEmpty(g_recogResult.lines)) {

                                String MRZ = g_recogResult.lines;

                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("MRZ", MRZ);
                                list.add(prodHashMap);


                            } else {
//                                    tv_key.setText(key + ":");
                                String mrz = value.replace(" ", "");
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("MRZ", mrz);
                                list.add(prodHashMap);

                            }
                            if (!TextUtils.isEmpty(g_recogResult.docType)) {

                                String Document = g_recogResult.docType;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("Document", Document);
                                list.add(prodHashMap);


                            }

                            if (!TextUtils.isEmpty(g_recogResult.surname)) {

                                String surname = g_recogResult.surname;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("surname", surname);
                                list.add(prodHashMap);
                            }

                            if (!TextUtils.isEmpty(g_recogResult.givenname)) {

                                String givenname = g_recogResult.givenname;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("givenname", givenname);
                                list.add(prodHashMap);
                            }
                            if (!TextUtils.isEmpty(g_recogResult.docnumber)) {

                                String docnumber = g_recogResult.docnumber;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("docnumber", docnumber);
                                list.add(prodHashMap);

                            }
                            if (!TextUtils.isEmpty(g_recogResult.docchecksum)) {

                                String docchecksum = g_recogResult.docchecksum;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("docchecksum", docchecksum);
                                list.add(prodHashMap);

                            }

                            if (!TextUtils.isEmpty(g_recogResult.country)) {

                                String country = g_recogResult.country;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("country", country);
                                list.add(prodHashMap);
                            }

                            if (!TextUtils.isEmpty(g_recogResult.nationality)) {

                                String nationality = g_recogResult.nationality;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("nationality", nationality);
                                list.add(prodHashMap);
                            }
                            if (!TextUtils.isEmpty(g_recogResult.sex)) {

                                String sex;
                                if (g_recogResult.sex.equalsIgnoreCase("F")) {

                                    sex = activity.getString(R.string.text_female);

                                } else {

                                    sex = activity.getString(R.string.text_male);


                                }

                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("sex", sex);
                                list.add(prodHashMap);
                            }

                            DateFormat date = new SimpleDateFormat("yymmdd", Locale.getDefault());
                            if (!TextUtils.isEmpty(g_recogResult.birth)) {
//                                    try {
//                                        Date birthDate = date.parse(g_recogResult.birth.replace("<", ""));
//                                        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yy");
//                                        String targetdatevalue = targetFormat.format(birthDate);

                                String birthDate = g_recogResult.birth;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("birthDate", birthDate);
                                list.add(prodHashMap);
//                                    } catch (ParseException e) {
//                                        e.printStackTrace();
//                                    }
                            }
                            if (!TextUtils.isEmpty(g_recogResult.birthchecksum)) {

                                String birthchecksum = g_recogResult.birthchecksum;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("birthchecksum", birthchecksum);
                                list.add(prodHashMap);

                            }
                            if (!TextUtils.isEmpty(g_recogResult.expirationchecksum)) {

                                String expirationchecksum = g_recogResult.expirationchecksum;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("expirationchecksum", expirationchecksum);
                                list.add(prodHashMap);

                            }
                            if (!TextUtils.isEmpty(g_recogResult.expirationdate)) {
//                                    try {
//                                        Date birthDate = date.parse(g_recogResult.expirationdate.replace("<", ""));
//                                        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yy");
//                                        String targetdatevalue = targetFormat.format(birthDate);

                                String expirationdate = g_recogResult.expirationdate;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("expiryDate", expirationdate);
                                list.add(prodHashMap);
//                                    } catch (ParseException e) {
//                                        e.printStackTrace();
//                                    }
                            }

                            if (!TextUtils.isEmpty(g_recogResult.otherid)) {

                                String otherid = g_recogResult.otherid;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("otherid", otherid);
                                list.add(prodHashMap);
                            }

                            if (!TextUtils.isEmpty(g_recogResult.otheridchecksum)) {

                                String otheridchecksum = g_recogResult.otheridchecksum;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("otheridchecksum", otheridchecksum);
                                list.add(prodHashMap);
                            }

                            if (!TextUtils.isEmpty(g_recogResult.secondrowchecksum)) {

                                String secondrowchecksum = g_recogResult.secondrowchecksum;
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("secondrowchecksum", secondrowchecksum);
                                list.add(prodHashMap);
                            }

                            String Result = "";

                            if (g_recogResult.ret == 0) {
                                Result = activity.getString(R.string.failed);
                            } else if (g_recogResult.ret == 1) {
                                Result = activity.getString(R.string.correct_mrz);
                            } else if (g_recogResult.ret == 2) {
                                Result = activity.getString(R.string.incorrect_mrz);
                            }

                            HashMap<String, String> prodHashMap = new HashMap<String, String>();
                            prodHashMap.put("Result", Result);
                            list.add(prodHashMap);

                            if (BackImage != null && !BackImage.isRecycled()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {


                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                            BackImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                                            String BackImageencoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);


                                            HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                            prodHashMap.put("BackImage", BackImageencoded);
                                            list.add(prodHashMap);

//                            iv_backside.setImageBitmap(BackImage);


//                            g_recogResult = new RecogResult();
////            mScanTitle.setText("Scan Front Side of " + RecogEngine.getCardname());
//                            SetFrontTemplete();
//                            isDone = false;
//                            RecogEngine.setFrontData(null);
//                            RecogEngine.setBackData(null);

//                            new CameraActivity(context, registrar, cameraid);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                        } else if (key.equalsIgnoreCase("PDF417")) {
                        }
                    }
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                    ly_back.setVisibility(View.GONE);

                    }
                });
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            messageChannel.send(list);
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }
    }

//    public int doWork() {
//
//        try {
//            RecogEngine recogEngine = new RecogEngine();
//            ans = recogEngine.initEngine(CameraActivity.this);
//        } catch (Exception e) {
//            Log.e("threadmessage", e.getMessage());
//        }
//        Toast.makeText(context, "sssss : " + ans, Toast.LENGTH_LONG);
//        return ans;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            mRecCnt = 0;
            gotmrz = -1;
            g_recogResult = new RecogResult();
//            mScanTitle.setText("Scan Front Side of " + RecogEngine.getCardname());
            SetFrontTemplete();
            isDone = false;
        }
    }

    @Override
    public void onDestroy() {

        // finalize the scan engine.
        if (mediaPlayer != null)
            mediaPlayer.release();
        Runtime.getRuntime().gc();
        mCardScanner.closeOCR(1);
        mCameraDevice.stopPreview();
        super.onDestroy();
        // unregister receiver.
    }


    @Override
    public void onBackPressed() {
        try {
            if (RecogEngine.getFrontimage() != null && !RecogEngine.getFrontimage().isRecycled())
                RecogEngine.getFrontimage().recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (RecogEngine.getBackimage() != null && !RecogEngine.getBackimage().isRecycled())
                RecogEngine.getBackimage().recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RecogEngine.setFrontData(null);
        RecogEngine.setBackData(null);
//        if (isBackPressed) {
        super.onBackPressed();
//        } else {
//            Toast.makeText(CameraActivity.this, "Press again to exit app", Toast.LENGTH_SHORT).show();
//            isBackPressed = true;
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                isBackPressed = false;
//            }
//        }, 2000);
    }


    /*When data scan successfully this method called and display result*/
    void showResultActivity() {
        Runtime.getRuntime().gc();
        if (!isDone) {
            isDone = true;
            new getDocData().execute();
            // pass result data through intent to resolve crash on face match
//            application.setMrzData(g_recogResult);
//            intent.putExtra("ocrData", application);
//            startActivityForResult(intent, 101);
            mCardScanner.closeOCR(0);

            playEffect();
        }
    }

    private void initializeCapabilities() {

        if (mCameraDevice != null)
            mInitialParams = mCameraDevice.getParameters();
//        mCameraDevice.autoFocus(new AutoFocusCallback());
        if (mInitialParams != null) {
            mInitialParams.getFocusMode();
            mFocusManager.initializeParameters(mInitialParams);
        }

        if (mCameraDevice != null) {
            mParameters = mCameraDevice.getParameters();
            GetCameraResolution();
        }
    }

    private void startPreview() {

        if (mCameraDevice != null) {
            if (mPausing || isFinishing())
                return;

            mCameraDevice.setErrorCallback(mErrorCallback);

            // If we're previewing already, stop the preview first (this will blank
            // the screen).
            if (mCameraState != PREVIEW_STOPPED)
                stopPreview();

            setPreviewDisplay(mSurfaceHolder);
            setDisplayOrientation();

            mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
            setCameraParameters(UPDATE_PARAM_ALL);

            // Inform the mainthread to go on the UI initialization.
            if (mCameraPreviewThread != null) {
                synchronized (mCameraPreviewThread) {
                    mCameraPreviewThread.notify();
                }
            }

            try {
                if (LOGV) Log.v(TAG, "startPreview");
                mCameraDevice.startPreview();
//                autoFocus();
            } catch (Throwable ex) {
                closeCamera();
//                throw new RuntimeException("startPreview failed", ex);
            }

            setCameraState(IDLE);

            // notify again to make sure main thread is wake-up.
            if (mCameraPreviewThread != null) {
                synchronized (mCameraPreviewThread) {
                    mCameraPreviewThread.notify();
                }
            }
        }
    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            if (mCameraDevice != null) {
                mCameraDevice.setPreviewDisplay(holder);
            }
        } catch (Throwable ex) {
//            closeCamera();
//            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

    private void setDisplayOrientation() {
        mDisplayRotation = Util.getDisplayRotation(activity);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation,
                mCameraId);
        mCameraDevice.setDisplayOrientation(mDisplayOrientation);
    }

    private void stopPreview() {
        if (mCameraDevice == null)
            return;
        mCameraDevice.stopPreview();
        // mCameraDevice.setPreviewCallback(null);
        setCameraState(PREVIEW_STOPPED);
    }

    private void setCameraState(int state) {
        mCameraState = state;
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            CameraHolder.instance().release();
            mCameraDevice.setErrorCallback(null);
            mCameraDevice = null;
            setCameraState(PREVIEW_STOPPED);
            mFocusManager.onCameraReleased();
        }
    }

    @Override
    protected void onPause() {
        if (LOGV) Log.e(TAG, "onPause");

        mOnResumePending = false;
        if(isDone==true) {
            mPausing = true;
        }
        mIsAutoFocusCallback = false;

        stopPreview();

        // Close the camera now because other activities may need to use it.
        closeCamera();
        resetScreenOn();

        // Remove the messages in the event queue.
        mHandler.removeMessages(FIRST_TIME_INIT);
        mHandler.removeMessages(TRIGER_RESTART_RECOG);

//		if (mFirstTimeInitialized)
//			mOrientationListener.disable();

        super.onPause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // Make sure we have a surface in the holder before proceeding.
        if (holder.getSurface() == null) {
            if (LOGV) Log.d(TAG, "holder.getSurface() == null");
            return;
        }

        // We need to save the holder for later use, even when the mCameraDevice
        // is null. This could happen if onResume() is invoked after this
        // function.
        mSurfaceHolder = holder;

        // The mCameraDevice will be null if it fails to connect to the camera
        // hardware. In this case we will show a dialog and then finish the
        // activity, so it's OK to ignore it.
        if (mCameraDevice == null)
            return;

        // Sometimes surfaceChanged is called after onPause or before onResume.
        // Ignore it.
        if (mPausing || isFinishing())
            return;

        // Set preview display if the surface is being created. Preview was
        // already started. Also restart the preview if display rotation has
        // changed. Sometimes this happens when the device is held in portrait
        // and camera app is opened. Rotation animation takes some time and
        // display rotation in onCreate may not be what we want.
        if (mCameraState == PREVIEW_STOPPED) {
            startPreview();
        } else {
            if (Util.getDisplayRotation(this) != mDisplayRotation) {
                setDisplayOrientation();
            }
            if (holder.isCreating()) {
                // Set preview display if the surface is being created and
                // preview
                // was already started. That means preview display was set to
                // null
                // and we need to set it now.
                setPreviewDisplay(holder);
            }
        }

        // If first time initialization is not finished, send a message to do
        // it later. We want to finish surfaceChanged as soon as possible to let
        // user see preview first.
        if (!mFirstTimeInitialized) {
            mHandler.sendEmptyMessage(FIRST_TIME_INIT);
        } else {
            initializeSecondTime();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCameraDevice != null) {
                Parameters p = mCameraDevice.getParameters();
                List<String> focusModes = p.getSupportedFocusModes();

//                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//                    mCameraDevice.autoFocus(new AutoFocusCallback());
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        stopPreview();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onShutter() {
        // TODO Auto-generated method stub
    }

    private void drawOverlay() {
        //draw white rectangle frame according to templete image aspect ratio
//        int panWidth = 465;
//        int panHeight= 296;
        int panWidth = (int) 300;
        imgheight = 191;
        int panHeight = imgheight;
//        float proportion = (float) panHeight / (float) panWidth;
//        if (proportion < 0.64) {
//            proportion = proportion + 0.03f;
//        }
//
//        int width = (int) ((dm.widthPixels * 5) / (float) 5.6f);
//        int height = (int) (width / 1.69f);
//
//        rectH = (int) height;
//        rectW = (int) width;


        float proportion = 0.636666667f;
        int width = (int) ((dm.widthPixels * 5) / (float) 5.6f);
        int height = (int) (width * proportion);

        rectH = (int) height;
        rectW = (int) width;
    }

    //set first templete from list by default
    private void SetTempleteImage() {
        if (setTemplateFirst("", -1)) {
            drawOverlay();
        }
    }

    private boolean setTemplateFirst(String cardside_, int i) {
        Mat mat = new Mat();
        PrimaryData data = RecogEngine.setPrimaryData(context, cardside_, i);
        if (data == null) {
            mat.release();
            return false;
        }
        cardpos = data.cardPos;
        cardside = data.cardSide;
//        openCvHelper.setTemplate(mat);
        if (cardside.contains("Front")) {
            isfront = true;
            isback = false;
        } else {
            isfront = false;
            isback = true;
        }
        return true;
    }

    private void SetBackTemplete() {
        cardside = "Backside";
        g_recogResult.lines = "";
        setTemplateFirst(cardside, -1);
    }

    private void SetFrontTemplete() {
        cardside = "FrontSide";
        g_recogResult.lines = "";
        setTemplateFirst(cardside, -1);
    }

    public void showToast(final String msg) {
        if (!isFinishing()) {
            if (isfront) {
                if (msg.contains("Blur detected over face")) {
                    if (!isTouchCalled && mParameters != null) {
                        String focusMode = mParameters.getFocusMode();
                        if (focusMode == null || Parameters.FOCUS_MODE_INFINITY.equals(focusMode)) {
                            return;
                        }

                        //                    if (e.getAction() == MotionEvent.ACTION_UP) {
                        isTouchCalled = true;
                        autoFocus();
//                        setCameraState(IDLE);

                        //                    }

                    }
                }
            }

//            Runnable runnable = new Runnable() {
//                public void run() {
//            mScanMsg.setText(msg);\


//            Toast.makeText(mContext, "" + msg, Toast.LENGTH_SHORT).show();
//                }
//            };
//            runOnUiThread(runnable);
//                drawOverlay();
//            }
//            }
        }
    }

    ObjectAnimator anim = null;

    //flip the image
    private void flipImage() {
        try {
//            mFlipImage.setVisibility(View.VISIBLE);
            anim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.flipping);
            anim.setTarget(mFlipImage);
            anim.setDuration(1000);


            Animator.AnimatorListener animatorListener
                    = new Animator.AnimatorListener() {

                public void onAnimationStart(Animator animation) {
                    try {
//                        mFlipImage.setVisibility(View.VISIBLE);
                        playEffect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void onAnimationRepeat(Animator animation) {

                }

                public void onAnimationEnd(Animator animation) {
                    try {
//                        mFlipImage.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void onAnimationCancel(Animator animation) {

                }
            };

            anim.addListener(animatorListener);
            anim.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateCameraParametersInitialize() {
        // Reset preview frame rate to the maximum because it may be lowered by
        // video camera RecogEngine.
        List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.max(frameRates);
            mParameters.setPreviewFrameRate(max);
        }

        //mParameters.setRecordingHint(false);

        // Disable video stabilization. Convenience methods not available in API
        // level <= 14
        String vstabSupported = mParameters
                .get("video-stabilization-supported");
        if ("true".equals(vstabSupported)) {
            mParameters.set("video-stabilization", "false");
        }
    }

    private void updateCameraParametersPreference() {

        // Since change scene mode may change supported values,

        //mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        mModeView.setText(R.string.preview_mode);

        int camOri = CameraHolder.instance().getCameraInfo()[mCameraId].orientation;
        // Set the preview frame aspect ratio according to the picture size.
        Camera.Size size = mParameters.getPictureSize();
        double aspectWtoH = 0.0;
        if ((camOri == 0 || camOri == 180) && size.height > size.width) {
            aspectWtoH = (double) size.height / size.width;
        } else {
            aspectWtoH = (double) size.width / size.height;
        }

        if (LOGV)
            Log.e(TAG, "picture width=" + size.width + ", height=" + size.height);

        // Set a preview size that is closest to the viewfinder height and has the right aspect ratio.
        List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
        Camera.Size optimalSize;
        //if (mode == SettingsActivity.CAPTURE_MODE)
        //	optimalSize = Util.getOptimalPreviewSize(this, sizes, aspectWtoH);
        //else
        {

//            Display display = getWindowManager().getDefaultDisplay();
//

//            WindowManager wm = (WindowManager)    context.getSystemService(Context.WINDOW_SERVICE);
//            Display display = wm.getDefaultDisplay();
//            Point psize = new Point();
//            display.getSize(psize);
//            width = psize.x;
//            height = psize.y;
//            int requiredArea = width * height;
            int requiredArea = mPreviewWidth * mPreviewHeight;
            //optimalSize = Util.getOptimalPreviewSize(this, sizes, aspectWtoH);
            optimalSize = Util.getOptimalPreviewSizeByArea(sizes, requiredArea);
        }

        // Camera.Size optimalSize = Util.getMaxPreviewSize(sizes, camOri);
        Camera.Size original = mParameters.getPreviewSize();

        if (LOGV) Log.i(TAG, " Sensor[" + mCameraId + "]'s orientation is " + camOri);
        if (!original.equals(optimalSize)) {
            if (camOri == 0 || camOri == 180) {
                mParameters.setPreviewSize(optimalSize.height, optimalSize.width);
            } else {
                mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
            }

            // Zoom related settings will be changed for different preview
            // sizes, so set and read the parameters to get lastest values

            if (mCameraDevice != null) {
                mCameraDevice.setParameters(mParameters);
                mParameters = mCameraDevice.getParameters();
            }
        }
        if (LOGV)
            Log.e(TAG, "Preview size is " + optimalSize.width + "x"
                    + optimalSize.height);

        String previewSize = "";
        previewSize = "[" + optimalSize.width + "x" + optimalSize.height + "]";
//        mPreviewSizeView.setText(previewSize);

        // Set JPEG quality.
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(
                mCameraId, CameraProfile.QUALITY_HIGH);
        mParameters.setJpegQuality(jpegQuality);

        // For the following settings, we need to check if the settings are
        // still supported by latest driver, if not, ignore the settings.

        //if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode))
        {
            if (mParameters != null) {
                // Set white balance parameter.
                String whiteBalance = "auto";
                if (isSupported(whiteBalance,
                        mParameters.getSupportedWhiteBalance())) {
                    mParameters.setWhiteBalance(whiteBalance);
                }

                String focusMode = mFocusManager.getFocusMode();
                mParameters.setFocusMode(focusMode);

                // Set exposure compensation
                int value = 0;
                int max = mParameters.getMaxExposureCompensation();
                int min = mParameters.getMinExposureCompensation();
                if (value >= min && value <= max) {
                    mParameters.setExposureCompensation(value);
                } else {
                    if (LOGV) Log.w(TAG, "invalid exposure range: " + value);
                }
            }
        }


        if (mParameters != null) {
            // Set flash mode.
            String flashMode = "off";
            List<String> supportedFlash = mParameters.getSupportedFlashModes();
            if (isSupported(flashMode, supportedFlash)) {
                mParameters.setFlashMode(flashMode);
            }

            if (LOGV) Log.e(TAG, "focusMode=" + mParameters.getFocusMode());
        }

    }

    public void GetCameraResolution() {
        if (mParameters != null && !isBlurSet) {
            isBlurSet = true;
            List sizes = mParameters.getSupportedPictureSizes();
            Camera.Size result = null;

            ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
            ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

            for (int i = 0; i < sizes.size(); i++) {
                result = (Camera.Size) sizes.get(i);
                arrayListForWidth.add(result.width);
                arrayListForHeight.add(result.height);
            }
            if (arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0) {
                // Gives Maximum Height
                int resolution = ((Collections.max(arrayListForWidth)) * (Collections.max(arrayListForHeight))) / 1024000;
                if (resolution >= 10) {
                    doblurcheck = true;
                } else {
                    doblurcheck = false;
                }
            }

            arrayListForWidth.clear();
            arrayListForHeight.clear();
        }
    }


    // We separate the parameters into several subsets, so we can update only
    // the subsets actually need updating. The PREFERENCE set needs extra
    // locking because the preference can be changed from GLThread as well.
    private void setCameraParameters(int updateSet) {
        if (mCameraDevice != null) {
            mParameters = mCameraDevice.getParameters();

            if ((updateSet & UPDATE_PARAM_INITIALIZE) != 0) {
                updateCameraParametersInitialize();
            }


            if ((updateSet & UPDATE_PARAM_PREFERENCE) != 0) {
                updateCameraParametersPreference();
                mIsAutoFocusCallback = false;
            }

            if (mParameters != null) {
                mCameraDevice.setParameters(mParameters);
                GetCameraResolution();
            }
        }
    }

    public void autoFocus() {

        if (mCameraDevice != null) {
            Parameters p = mCameraDevice.getParameters();
            List<String> focusModes = p.getSupportedFocusModes();

            if (focusModes != null && focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
                if (FocusManager.isSupported(Parameters.FOCUS_MODE_AUTO, mParameters.getSupportedFocusModes())) {
                    mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                    if (mCameraDevice != null) {
                        mCameraDevice.setParameters(mParameters);
                        mCameraDevice.autoFocus(mAutoFocusCallback);
                        setCameraState(FOCUSING);
                        isTouchCalled = false;
                    }
                }
            } else {
                isTouchCalled = false;
            }
        }

    }

    @Override
    public void cancelAutoFocus() {
        // TODO Auto-generated method stub
        mCameraDevice.cancelAutoFocus();
        if (mCameraState != SELFTIMER_COUNTING
                && mCameraState != SNAPSHOT_IN_PROGRESS) {
            setCameraState(IDLE);
        }
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
        isTouchCalled = false;
    }

    @Override
    public boolean capture() {
        // If we are already in the middle of taking a snapshot then ignore.
        if (mCameraState == SNAPSHOT_IN_PROGRESS || mCameraDevice == null) {
            return false;
        }
        setCameraState(SNAPSHOT_IN_PROGRESS);

        return true;
    }

    @Override
    public void setFocusParameters() {
        // TODO Auto-generated method stub
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
    }

    @Override
    public void playSound(int soundId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
//        if (mPausing || mCameraDevice == null || !mFirstTimeInitialized
//                || mCameraState == SNAPSHOT_IN_PROGRESS
//                || mCameraState == PREVIEW_STOPPED
//                || mCameraState == SAVING_PICTURES) {
//            return false;
//        }
        if (!isTouchCalled) {
            String focusMode = mParameters.getFocusMode();
            if (focusMode == null || Parameters.FOCUS_MODE_INFINITY.equals(focusMode)) {
                return false;
            }

            if (e.getAction() == MotionEvent.ACTION_UP) {
                isTouchCalled = true;
                autoFocus();
            }

        }

        //
        //return mFocusManager.onTouch(e);

        return true;
    }

    // If the Camera is idle, update the parameters immediately, otherwise
    // accumulate them in mUpdateSet and update later.
    private void setCameraParametersWhenIdle(int additionalUpdateSet) {
        mUpdateSet |= additionalUpdateSet;
        if (mCameraDevice == null) {
            // We will update all the parameters when we open the device, so
            // we don't need to do anything now.
            mUpdateSet = 0;
            return;
        } else if (isCameraIdle()) {
            setCameraParameters(mUpdateSet);
            mUpdateSet = 0;
        } else {
            if (!mHandler.hasMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
                mHandler.sendEmptyMessageDelayed(
                        SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
            }
        }
    }

    private boolean isCameraIdle() {
        return (mCameraState == IDLE || mFocusManager.isFocusCompleted());
    }

    void playEffect() {
        if (audioManager != null)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer1) {
//                mediaPlayer.stop();
//                mediaPlayer.release();
            }
        });
    }

    //requesting the camera permission
    public void requestCameraPermission(FlutterEngine flutterEngine) {

        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            1);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
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
                            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(CameraActivity.this, per);
                            if (!showRationale) {
                                //user also CHECKED "never ask again"
                                if (per.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showRationaleCamera = true;
                                } else if (per.equals(Manifest.permission.CAMERA)) {
                                    showRationaleStorage = true;
                                }
                            } else if (per.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                callCamera = true;
                            } else if (per.equals(Manifest.permission.CAMERA)) {
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
                            mCardScanner.initEngine(mContext, this);
                        }

                        dm = getResources().getDisplayMetrics();
                        ///audio init
                        mediaPlayer = MediaPlayer.create(CameraActivity.this, R.raw.beep);
                        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                        methodChannel = new MethodChannel(class_flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
                        methodChannel.setMethodCallHandler(this);
                        return;
                    }
                    if (callCamera || callStorage) {
                        requestCameraPermission(class_flutterEngine);
                    }
            }
        } else {
            requestCameraPermission(class_flutterEngine);
        }
    }

    @Override
    public void onUpdateProcess(final String s) {
        framemessage = s;



//        Runnable runnable = new Runnable() {
//            public void run() {
//                if (!isFinishing()) {
//
//                }
//            }
//        };
//        runOnUiThread(runnable);

        if (!s.isEmpty()) {
            newMessage = s;
            CameraActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                            showToast("Keep Document Steady");
                    HashMap<String, String> prodHashMap = new HashMap<String, String>();
                    prodHashMap.put("message", framemessage);
                    list.add(prodHashMap);
                    messageChannel.send(list);
                }
            });
            if (!s.contains("Processing")) {
                final Runnable runnable = () -> {
                    try {
                        if (!s.contains("lighting"))
                            if (newMessage.equals(s) || !s.contains("Processing")) {
                                HashMap<String, String> prodHashMap = new HashMap<String, String>();
                                prodHashMap.put("message", "");
                                list.add(prodHashMap);
                                messageChannel.send(list);
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                Runnable run = () -> new Handler().postDelayed(runnable, 1000);
                if (!isFinishing()) {
                    runOnUiThread(run);
                }
            }
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEAR_SCREEN_DELAY: {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                }
                case FIRST_TIME_INIT: {
                    initializeFirstTime();
                    break;
                }

                case SET_CAMERA_PARAMETERS_WHEN_IDLE: {
                    setCameraParametersWhenIdle(0);
                    break;
                }

//                case TRIGER_RESTART_RECOG:
//                    if (!mPausing)
//                        mCameraDevice.setOneShotPreviewCallback(CameraActivity.this);
//                    // clearNumberAreaAndResult();
//                    break;
            }
        }
    }

    private final class FaceCallbackclass implements FaceCallback {

        @Override
        public void onInitEngine(int ret) {

        }

        @Override
        public void onLeftDetect(FaceDetectionResult face) {

        }

        @Override
        public void onRightDetect(FaceDetectionResult face) {

        }

        @Override
        public void onExtractInit(int ret) {

        }
    }

    private final class AutoFocusCallback implements
            Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {
            if (mPausing)
                return;

            if (mCameraState == FOCUSING) {
                setCameraState(IDLE);
            }
            mFocusManager.onAutoFocus(focused);
            mIsAutoFocusCallback = true;

            String focusMode = mFocusManager.getFocusMode();
            mParameters.setFocusMode(focusMode);
            mCameraDevice.setParameters(mParameters);
            isTouchCalled = false;
//            autoFocus();
        }
    }

    public class CameraErrorCallback implements Camera.ErrorCallback {
        private static final String TAG = "CameraErrorCallback";

        public void onError(int error, Camera camera) {
            if (LOGV) Log.e(TAG, "Got camera error callback. error=" + error);
            if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
                // We are not sure about the current state of the app (in preview or
                // snapshot or recording). Closing the app is better than creating a
                // new Camera object.
                throw new RuntimeException("Media server died.");
            }
        }

    }

    public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height,
                                                byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }

    public static Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        Bitmap bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return bmp;
    }


}
