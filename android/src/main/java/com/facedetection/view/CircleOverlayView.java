package com.facedetection.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.facedetection.common.BitmapUtils;

public class CircleOverlayView extends View {

    private final Object lock = new Object();
    private DisplayMetrics dm;
    private int width, height;
    private int color = 0xFF8BA9F4;

    private static final float BOX_STROKE_WIDTH = 7.0f;

    public CircleOverlayView(Context context) {
        super(context);
        init(context);
    }

    public CircleOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        dm = Resources.getSystem().getDisplayMetrics();
        width = (int) (dm.widthPixels / BitmapUtils.WIDTH_RATIO);
        height = (int) (width * BitmapUtils.HEIGHT_RATIO);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (lock) {

            createWindowFrame(canvas);
        }
    }

    protected void createWindowFrame(Canvas canvas) {

        RectF outerRectangle = new RectF(0, 0, dm.widthPixels, dm.heightPixels);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setAlpha(255);
        canvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        int croppedWidth = width;
        int croppedHeight = height;
        Point centerOfCanvas = new Point(dm.widthPixels / 2, (dm.heightPixels / 2)-(int)((dm.heightPixels - canvas.getHeight())*0.5));
        int left = centerOfCanvas.x - (croppedWidth / 2);
        int top = centerOfCanvas.y - (croppedHeight / 2);
        int right = centerOfCanvas.x + (croppedWidth / 2);
        int bottom = centerOfCanvas.y + (croppedHeight / 2);

        RectF rectF = new RectF(left, top, right, bottom);
//        Logger.e(CircleOverlayView.class.getSimpleName(), rectF.toString() + " (w,h) : (" + rectF.width() + "," + rectF.height() + ")");
        canvas.drawOval(rectF, paint);

    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

}