package com.accurascan.accuraemirates;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.docrecog.scan.ImageOpencv;
import com.docrecog.scan.RecogEngine;
import com.docrecog.scan.RecogResult;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import static org.opencv.BuildConfig.DEBUG;


public class ViewDataActivityTEMP extends AppCompatActivity implements View.OnClickListener, FaceCallback {

    private final String TAG = "ViewDataActivityTEMP";
    FaceDetectionResult leftResult = null;
    FaceDetectionResult rightResult = null;
    float match_score = 0.0f;
    Bitmap face2 = null;
    Bitmap face1;
    final private int CAPTURE_IMAGE = 2;
    String mrzdata = "";
    private ImageView ivUserProfile;
    private ImageView ivUserProfile2;
    private ImageView ivUserProfile3;
    private TextView tvSave, tvFM, tvCancel, tvCancel1,
            tvAuth, tvLivenessAuthFacemap, tvLivenessEnrollFacemap, tvLivenessScore, tvLivenessAuthResultFacemap,
            tvLivenessEnrollResultFacemap, tvMatchScore, tvFaceMatchScore1, tvRetry;
    private LinearLayout llFacemap, llLiveness, llFaceMatchScore;
    private File imageFile;
    private byte[] bytes;
    private String sessionId;
    private RecogResult g_recogResult;
    //custom
    ImageView iv_holder_image;
    RecyclerView ry_cardresult;
    TableLayout mrz_table_layout, front_table_layout, back_table_layout, security_table_layout, barcode_table_layout;
//    AccuraDemoApplication application = AccuraDemoApplication.getInstance();
    ImageView iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    String type;
//    private RecogEngine mCardScanner;
    View ly_mrz_container, ly_front_container, ly_back_container, ly_security_container, ly_barcode_container;
    String[][] Frontdata;
    String[][] Backdata;
    int isdonecount = 0;


    // convert from bitmap to byte array
    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data_temp); //memory leak

        initEngine(); //initialize the engine
//        if (mCardScanner == null) {
//            mCardScanner = new RecogEngine();
//            mCardScanner.initEngine(getApplicationContext(), ViewDataActivityTEMP.this);
//        }
        //call zoom connect API

        initUI();
//        addScannedDataItem();
        getDocumentdata();//memory leak
    }

    private void getDocumentdata() {

        Frontdata = RecogEngine.getFrontData();
        Backdata = RecogEngine.getBackData();
        final LayoutInflater inflater = LayoutInflater.from(ViewDataActivityTEMP.this);
        if (Frontdata != null && Backdata != null) {
            ly_front_container.setVisibility(View.GONE);
            ly_back_container.setVisibility(View.VISIBLE);
        }

        if (Frontdata != null) {
            final Bitmap frontBitmap = RecogEngine.getFrontimage();
            for (int i = 0; i < Frontdata.length; i++) {

//                TableRow tr =  new TableRow(this);
                final String[] array = Frontdata[i];

                Runnable runnable = new Runnable() {
                    public void run() {
                        if (array != null && array.length != 0) {
                            final String key = array[0];
                            Log.e(TAG, "run: key" + key);
                            //                key.replace("img11", "");
                            final String value = array[1];
                            final View layout = (View) inflater.inflate(R.layout.table_row, null);
                            //            TableRow tableRow = layout.findViewById(R.id.table_row);
                            final TextView tv_key = layout.findViewById(R.id.tv_key);
                            final TextView tv_value = layout.findViewById(R.id.tv_value);
                            ImageView imageView = layout.findViewById(R.id.iv_image);

                            tv_key.setTextSize(16);
                            tv_key.setTextColor(getResources().getColor(R.color.black));
                            if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {

                                tv_key.setText(key.replace("_img", "") + ":");
                                // if it contains , then its image else it is string
                                if (!key.contains("_img")) {
                                    //                TextView tv_value =new TextView(this);
                                    tv_value.setText(value);
                                    imageView.setVisibility(View.GONE);
                                    front_table_layout.addView(layout);

                                    //                tableRow.addView(tv_key);
                                    //                tableRow.addView(tv_value);

                                } else {
                                    try {
                                        if (key.toLowerCase().contains("face")) {
                                            try {
                                                if (face1 == null) {
                                                    face1 = ImageOpencv.getImage(value); //memory leak
                                                }
                                                if (DEBUG) { // TODO remove it
                                                    if (face1 == null) {
                                                        face1 = g_recogResult.faceBitmap; //memory leak
                                                    }
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            tv_value.setVisibility(View.GONE);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }


                        }
                    }
                };
                runOnUiThread(runnable);
                if (frontBitmap != null && !frontBitmap.isRecycled()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                iv_frontside.setImageBitmap(frontBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }

            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ly_front.setVisibility(View.GONE);

                }
            });
        }

        if (Backdata != null) {
            final Bitmap BackImage = RecogEngine.getBackimage();

            Runnable runnable = new Runnable() {
                public void run() {
                    for (int i = 0; i < Backdata.length; i++) {
                        View layout = (View) inflater.inflate(R.layout.table_row, null);
                        //            TableRow tableRow = layout.findViewById(R.id.table_row);
                        TextView tv_key = layout.findViewById(R.id.tv_key);
                        TextView tv_value = layout.findViewById(R.id.tv_value);
                        ImageView imageView = layout.findViewById(R.id.iv_image);
                        String[] array = Backdata[i];

                        if (array != null && array.length != 0) {
                            //                    back_table_layout.setVisibility(View.VISIBLE);
                            String key = array[0];
                            //if element is image then key = key+"img11" so remove it
                            String value = array[1];
                            if (!key.equalsIgnoreCase("mrz") && !key.toLowerCase().contains(".png")) {
                                if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                    tv_key.setTextSize(16);
                                    tv_key.setTextColor(getResources().getColor(R.color.black));
                                    if (!key.contains("_img")) {
                                        tv_value.setText(value);
                                        imageView.setVisibility(View.GONE);
                                        back_table_layout.addView(layout);
                                    } else {
                                        try {
                                            Bitmap bitmap = ImageOpencv.getImage(value);
                                            if (bitmap != null)
                                                imageView.setImageBitmap(ImageOpencv.getImage(value)); //memory leak
                                            tv_value.setVisibility(View.GONE);
                                            back_table_layout.addView(layout);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    tv_key.setText(key.replace("_img", "") + ":");
                                }
                            } else if (key.toLowerCase().contains("mrz")) {
                                ly_mrz_container.setVisibility(View.VISIBLE);
                                //get data from mrz
                                int ret = 0;
                                int faceret = 0; // detecting face return value
                                Bitmap bmCard;
                                int mRecCnt = 0;
                                int mDisplayRotation = 0;
                                if (!TextUtils.isEmpty(g_recogResult.lines)) {
                                    View layout1 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key1 = layout1.findViewById(R.id.tv_key);
                                    TextView tv_value1 = layout1.findViewById(R.id.tv_value);
                                    tv_key1.setText("MRZ" + ":");
                                    //                        String str = "LAST NAME" + ":" + g_recogResult.surname + "<br/>";
                                    //                        mrzdata = mrzdata + str;

                                    tv_value1.setText(g_recogResult.lines);
                                    //                            scanData.setLastName(g_recogResult.surname);
                                    mrz_table_layout.addView(layout1);

                                } else {
                                    tv_key.setText(key + ":");
                                    String mrz = value.replace(" ", "");

                                    tv_value.setText(mrz);
                                    mrz_table_layout.addView(layout);
                                }
                                if (!TextUtils.isEmpty(g_recogResult.docType)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);//memory leak
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("DOC TYPE" + ":");
                                    String str = "Document" + ":" + g_recogResult.docType + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.docType);
                                    mrz_table_layout.addView(layout5);

                                }

                                if (!TextUtils.isEmpty(g_recogResult.surname)) {
                                    View layout1 = (View) inflater.inflate(R.layout.table_row, null);//memory leak
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key1 = layout1.findViewById(R.id.tv_key);
                                    TextView tv_value1 = layout1.findViewById(R.id.tv_value);
                                    tv_key1.setText("Last Name" + ":");
                                    String str = "LAST NAME" + ":" + g_recogResult.surname + "<br/>";
                                    mrzdata = mrzdata + str;

                                    tv_value1.setText(g_recogResult.surname);
                                    //                            scanData.setLastName(g_recogResult.surname);
                                    mrz_table_layout.addView(layout1);

                                }

                                if (!TextUtils.isEmpty(g_recogResult.givenname)) {
                                    View layout3 = (View) inflater.inflate(R.layout.table_row, null);//memory leak
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key3 = layout3.findViewById(R.id.tv_key);
                                    TextView tv_value3 = layout3.findViewById(R.id.tv_value);
                                    tv_key3.setText("FIRST NAME" + ":");
                                    String str = "First Name" + ":" + g_recogResult.givenname + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value3.setText(g_recogResult.givenname);
                                    //                            scanData.setFirstName(g_recogResult.givenname);
                                    mrz_table_layout.addView(layout3);

                                }
                                if (!TextUtils.isEmpty(g_recogResult.docnumber)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("DOC NUMBER" + ":");
                                    String str = "Document No" + ":" + g_recogResult.docnumber + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.docnumber);
                                    mrz_table_layout.addView(layout5);

                                }
                                if (!TextUtils.isEmpty(g_recogResult.docchecksum)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("DOC CHECKSUM" + ":");
                                    String str = "Document Check Number" + ":" + g_recogResult.docchecksum + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.docchecksum);
                                    mrz_table_layout.addView(layout5);

                                }

                                //                        if (!TextUtils.isEmpty(g_recogResult.docnumber)) {
                                //                            tv_value.setText(g_recogResult.docnumber);
                                //                            scanData.setPassportNo(g_recogResult.docnumber);
                                //                        }

                                if (!TextUtils.isEmpty(g_recogResult.country)) {
                                    View layout4 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key4 = layout4.findViewById(R.id.tv_key);
                                    TextView tv_value4 = layout4.findViewById(R.id.tv_value);
                                    tv_key4.setText("COUNTRY" + ":");
                                    String str = "Country" + ":" + g_recogResult.country + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value4.setText(g_recogResult.country);
                                    //                            scanData.setCountry(g_recogResult.country);
                                    mrz_table_layout.addView(layout4);

                                }

                                if (!TextUtils.isEmpty(g_recogResult.nationality)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("NATIONALITY" + ":");
                                    String str = "Nationality" + ":" + g_recogResult.nationality + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.nationality);
                                    mrz_table_layout.addView(layout5);

                                }
                                if (!TextUtils.isEmpty(g_recogResult.sex)) {
                                    View layout6 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key6 = layout6.findViewById(R.id.tv_key);
                                    TextView tv_value6 = layout6.findViewById(R.id.tv_value);
                                    if (g_recogResult.sex.equalsIgnoreCase("F")) {
                                        tv_key6.setText("SEX" + ":");
                                        String str = "Sex" + ":" + getString(R.string.text_female) + "<br/>";
                                        mrzdata = mrzdata + str;
                                        tv_value6.setText(getString(R.string.text_female));
                                        //                                scanData.setGender(getString(R.string.text_female));
                                        mrz_table_layout.addView(layout6);

                                    } else {
                                        tv_key6.setText("SEX" + ":");

                                        tv_value6.setText(getString(R.string.text_male));
                                        String str = "SEX" + ":" + getString(R.string.text_male) + "<br/>";
                                        mrzdata = mrzdata + str;
                                        //                                scanData.setGender(getString(R.string.text_male));
                                        mrz_table_layout.addView(layout6);

                                    }
                                }

                                DateFormat date = new SimpleDateFormat("yymmdd", Locale.getDefault());
                                SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-mm-yy", Locale.getDefault());
                                if (!TextUtils.isEmpty(g_recogResult.birth)) {
                                    try {
                                        View layout7 = (View) inflater.inflate(R.layout.table_row, null);
                                        //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                        TextView tv_key7 = layout7.findViewById(R.id.tv_key);
                                        TextView tv_value7 = layout7.findViewById(R.id.tv_value);
                                        tv_key7.setText("DATE OF BIRTH" + ":");

                                        Date birthDate = date.parse(g_recogResult.birth.replace("<", ""));
                                        tv_value7.setText(newDateFormat.format(birthDate));
                                        String str = "Date of Birth" + ":" + newDateFormat.format(birthDate) + "<br/>";
                                        mrzdata = mrzdata + str;
                                        //                                scanData.setDateOfBirth(newDateFormat.format(birthDate));
                                        mrz_table_layout.addView(layout7);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (!TextUtils.isEmpty(g_recogResult.birthchecksum)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("BIRTH CHECKSUM" + ":");
                                    String str = "Birth Check Number" + ":" + g_recogResult.birthchecksum + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.birthchecksum);
                                    mrz_table_layout.addView(layout5);

                                }
                                if (!TextUtils.isEmpty(g_recogResult.expirationchecksum)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("EXPIRATION CHECKSUM" + ":");
                                    String str = "Expiration Check Number" + ":" + g_recogResult.expirationchecksum + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.expirationchecksum);
                                    mrz_table_layout.addView(layout5);

                                }
                                if (!TextUtils.isEmpty(g_recogResult.expirationdate)) {
                                    try {
                                        View layout8 = (View) inflater.inflate(R.layout.table_row, null); //memory leak
                                        //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                        TextView tv_key8 = layout8.findViewById(R.id.tv_key);
                                        TextView tv_value8 = layout8.findViewById(R.id.tv_value);
                                        tv_key8.setText("EXPIRY DATE" + ":");

                                        Date expiryDate = date.parse(g_recogResult.expirationdate);
                                        tv_value8.setText(newDateFormat.format(expiryDate));
                                        String str = "Date Of Expiry" + ":" + newDateFormat.format(expiryDate) + "<br/>";
                                        mrzdata = mrzdata + str;
                                        //                                scanData.setDateOfExpiry(newDateFormat.format(expiryDate));
                                        mrz_table_layout.addView(layout8);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (!TextUtils.isEmpty(g_recogResult.otherid)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("OTHER ID" + ":");
                                    String str = "OTHER ID" + ":" + g_recogResult.otherid + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.otherid);
                                    mrz_table_layout.addView(layout5);

                                }

                                if (!TextUtils.isEmpty(g_recogResult.otheridchecksum)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null);////memory leak
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("OTHER ID CHECKSUM" + ":");
                                    String str = "Other ID Check" + ":" + g_recogResult.otheridchecksum + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.otheridchecksum);
                                    mrz_table_layout.addView(layout5);

                                }

                                if (!TextUtils.isEmpty(g_recogResult.secondrowchecksum)) {
                                    View layout5 = (View) inflater.inflate(R.layout.table_row, null); //memory leak
                                    //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                    TextView tv_key5 = layout5.findViewById(R.id.tv_key);
                                    TextView tv_value5 = layout5.findViewById(R.id.tv_value);
                                    tv_key5.setText("SECOND ROW CHECKSUM" + ":");
                                    String str = "Second Row Check Number " + ":" + g_recogResult.secondrowchecksum + "<br/>";
                                    mrzdata = mrzdata + str;
                                    tv_value5.setText(g_recogResult.secondrowchecksum);
                                    mrz_table_layout.addView(layout5);

                                }
                                View layout6 = (View) inflater.inflate(R.layout.table_row, null); //memory leak
                                //            TableRow tableRow = layout.findViewById(R.id.table_row);
                                TextView tv_key5 = layout6.findViewById(R.id.tv_key);
                                TextView tv_value5 = layout6.findViewById(R.id.tv_value);
                                tv_key5.setText("Result" + ":");

                                //                        tv_value5.setText(g_recogResult.secondrowchecksum);
                                if (g_recogResult.ret == 0) {
                                    tv_value5.setText(getString(R.string.failed));
                                    String str = "Result " + ":" + getString(R.string.failed) + "<br/>";
                                    mrzdata = mrzdata + str;
                                    //                            scanData.setStatus(getString(R.string.failed));
                                } else if (g_recogResult.ret == 1) {
                                    tv_value5.setText(getString(R.string.correct_mrz));
                                    String str = "Result" + ":" + getString(R.string.correct_mrz) + "<br/>";
                                    mrzdata = mrzdata + str;
                                    //                            scanData.setStatus(getString(R.string.correct_mrz));
                                } else if (g_recogResult.ret == 2) {
                                    tv_value5.setText(getString(R.string.incorrect_mrz));
                                    String str = "Result " + ":" + getString(R.string.incorrect_mrz) + "<br/>";
                                    mrzdata = mrzdata + str;
                                    //                            scanData.setStatus(getString(R.string.incorrect_mrz));
                                }
                                mrz_table_layout.addView(layout6);
                            } else if (key.equalsIgnoreCase("PDF417")) {
                            }
                        }
                    }
                }
            };
            runOnUiThread(runnable);//memory leak
            if (BackImage != null && !BackImage.isRecycled()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            iv_backside.setImageBitmap(BackImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ly_back.setVisibility(View.GONE);

                }
            });
        }

       /* if (Build.VERSION.SDK_INT >= 11) {
            //--post GB use serial executor by default --
            new SetUpAllFrontData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{});
        } else {
            //--GB uses ThreadPoolExecutor by default--
            new SetUpAllFrontData().execute(new Void[]{});
        }*/
       /* if (Build.VERSION.SDK_INT >= 11) {
            //--post GB use serial executor by default --
            new SetUpAllBackData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{});
        } else {
            //--GB uses ThreadPoolExecutor by default--
            new SetUpAllBackData().execute(new Void[]{});
        }*/

        setData(); //set the result data
    }

    private void initUI() {
        //initialize the UI
        ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile2 = findViewById(R.id.ivUserProfile2);
        ivUserProfile3 = findViewById(R.id.ivUserProfile3);
        tvAuth = findViewById(R.id.tvAuth);
        tvRetry = findViewById(R.id.tvRetry);
        tvFM = findViewById(R.id.tvFM);
        tvFaceMatchScore1 = findViewById(R.id.tvFaceMatchScore1);
        tvLivenessAuthFacemap = findViewById(R.id.tvLivenessAuthFacemap);
        tvLivenessEnrollFacemap = findViewById(R.id.tvLivenessEnrollFacemap);
        tvLivenessScore = findViewById(R.id.tvLivenessScore);
        tvLivenessAuthResultFacemap = findViewById(R.id.tvLivenessAuthResultFacemap);
        tvLivenessEnrollResultFacemap = findViewById(R.id.tvLivenessEnrollResultFacemap);
        tvMatchScore = findViewById(R.id.tvMatchScore);
        tvSave = findViewById(R.id.tvSave);
        tvCancel = findViewById(R.id.tvCancel);
        tvCancel1 = findViewById(R.id.tvCancel1);

        llLiveness = findViewById(R.id.llLiveness);
        llFaceMatchScore = findViewById(R.id.llFaceMatchScore);
        llFacemap = findViewById(R.id.llFacemap);

        tvSave.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvFM.setOnClickListener(this);
        tvCancel1.setOnClickListener(this);

//        if (AccuraDemoApplication.getmMenuMode() == AccuraDemoApplication.MENU_MODE_OCR) {
//        } else {
        tvFM.setVisibility(View.VISIBLE);
        tvCancel.setVisibility(View.VISIBLE);
        tvCancel1.setVisibility(View.GONE);
//        }
        tvSave.setVisibility(View.GONE);
        //custom
        ly_back = findViewById(R.id.ly_back);
        ly_front = findViewById(R.id.ly_front);
//        iv_holder_image = findViewById(R.id.iv_holder_image);
        iv_frontside = findViewById(R.id.iv_frontside);
        iv_backside = findViewById(R.id.iv_backside);
        ry_cardresult = findViewById(R.id.ry_cardresult);
//        table_layout = findViewById(R.id.table_layout);
        mrz_table_layout = findViewById(R.id.mrz_table_layout);
        front_table_layout = findViewById(R.id.front_table_layout);
        back_table_layout = findViewById(R.id.back_table_layout);
        security_table_layout = findViewById(R.id.security_table_layout);
        barcode_table_layout = findViewById(R.id.barcode_table_layout);

        ly_mrz_container = findViewById(R.id.ly_mrz_container);
        ly_front_container = findViewById(R.id.ly_front_container);
        ly_back_container = findViewById(R.id.ly_back_container);
        ly_security_container = findViewById(R.id.ly_security_container);
        ly_barcode_container = findViewById(R.id.ly_barcode_container);


    }

    private void setData() {
        try {
            if (face1 != null && !face1.isRecycled()) {
                ivUserProfile.setImageBitmap(face1); //memory leak
                ivUserProfile.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //handle click of different view
    @Override
    public void onClick(View view) {
//        isFaceMatch = false;

        int id = view.getId();
        if (id == R.id.tvFM) { // handle click of FaceMatch
            //Start Facematch
//                isFaceMatch = true;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
            Uri uriForFile = FileProvider.getUriForFile(
                    ViewDataActivityTEMP.this,
                    getPackageName() + ".provider",
                    f
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
            startActivityForResult(intent, CAPTURE_IMAGE);
        } else if (id == R.id.tvCancel) {
            tvCancel.setClickable(false);
            tvCancel.setFocusable(false);
            onBackPressed();
        } else if (id == R.id.tvCancel1) {
            tvCancel.setClickable(false);
            tvCancel.setFocusable(false);
            onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // make sure the result was returned correctly
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE) {
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

                Bitmap bmp = rotateImage(ttt.getAbsolutePath());
                ttt.delete();
                face2 = bmp.copy(Bitmap.Config.ARGB_8888, true);
                ivUserProfile2.setImageBitmap(face2);
                ivUserProfile2.setVisibility(View.VISIBLE);

                //if (g_recogResult.faceBitmap != null) {
                if (face1 != null && !face1.isRecycled()) {
                    //Bitmap nBmp = g_recogResult.faceBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Bitmap nBmp = face1.copy(Bitmap.Config.ARGB_8888, true);
                    int w = nBmp.getWidth();
                    int h = nBmp.getHeight();
                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);
                    FaceLockHelper.DetectLeftFace(buff.array(), w, h);
                }

                if (face2 != null && !face2.isRecycled()) {
                    Bitmap nBmp = face2;
                    int w = nBmp.getWidth();
                    int h = nBmp.getHeight();
                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);
                    FaceLockHelper.DetectRightFace(buff.array(), w, h, null);
                }
            }
        }
    }

    //used for rotate image of given path
    //parameter to pass : String path
    // return bitmap
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

    //Get bitmap image form path
    //parameter to pass : String path
    // return Bitmap
    private Bitmap decodeFileFromPath(String path) {
        Uri uri = getImageUri(path);
        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(uri);

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
            in = getContentResolver().openInputStream(uri);
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

    //get image uwi from given string path
    //parameter to pass : String path
    // return Uri
    private Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    //Used for resizing bitmap
    //parameter to pass : Bitmap image, int maxSize
    // return bitmap
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

    //checked for user permission
    private boolean checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(ViewDataActivityTEMP.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return false;
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 111) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //resume tasks needing this permission
                checkReadWritePermission();
            }
        }
    }

    //initialize the engine
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

    @Override
    public void onInitEngine(int ret) {
    }

    // call if face detect
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        leftResult = null;
        if (faceResult != null) {
            leftResult = faceResult;

            if (face2 != null && !face2.isRecycled()) {
                Bitmap nBmp = face2.copy(Bitmap.Config.ARGB_8888, true);
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
                }
            }
        } else {
            if (face2 != null && !face2.isRecycled()) {
                Bitmap nBmp = face2.copy(Bitmap.Config.ARGB_8888, true);
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
                }
            }
        }
        calcMatch();
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            rightResult = faceResult;
        } else {
            rightResult = null;
        }
        calcMatch();
    }

    @Override
    public void onExtractInit(int ret) {
    }

    //Calculate Pan and Adhare facematch score
    public void calcMatch() {
        if (leftResult == null || rightResult == null) {
            match_score = 0.0f;
        } else {
            match_score = FaceLockHelper.Similarity(leftResult.getFeature(), rightResult.getFeature(), rightResult.getFeature().length);
            match_score *= 100.0f;
        }
        tvFaceMatchScore1.setText(String.valueOf(match_score));
        llFaceMatchScore.setVisibility(View.VISIBLE);
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
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    @Override
    public void onBackPressed() {
//        image.recycle();
        /*if (imageFile.exists()) {
            imageFile.delete();
        }*/

        try {
            RecogEngine.getFrontimage().recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            RecogEngine.getBackimage().recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            RecogEngine.setFrontimage(null);
            RecogEngine.setBackimage(null);
            RecogEngine.setFrontData(null);
            RecogEngine.setBackData(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        startActivity(new Intent(ViewDataActivityTEMP.this, CameraActivity.class));
//        finishAffinity();

        setResult(RESULT_OK);
        super.onBackPressed();
    }
    //mail OCR,FM and Liveness result

    private int pixelDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }

}
