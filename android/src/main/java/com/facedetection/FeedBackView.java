package com.facedetection;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class FeedBackView extends Drawable {
    private Paint paint;

    private RectF bounds = new RectF();

    private int width;
    private int height;

    private FeedBackShadow feedBackShadow;
    private int shadowOffset;

    private RectF drawRect;

    private float rx;
    private float ry;

    public FeedBackView(FeedBackShadow feedBackShadow, int color, float rx, float ry) {
        this.feedBackShadow = feedBackShadow;
        shadowOffset = this.feedBackShadow.getShadowOffset();

        this.rx = rx;
        this.ry = ry;

        paint = new Paint();
        paint.setAntiAlias(true);
        /**
         * Solve the problem of aliasing when rotating
         */
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        /**
         * Set shadow
         */
        paint.setShadowLayer(feedBackShadow.getShadowRadius(), feedBackShadow.getShadowDx(), feedBackShadow.getShadowDy(), feedBackShadow.getShadowColor());

        drawRect = new RectF();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (bounds.right - bounds.left > 0 && bounds.bottom - bounds.top > 0) {
            this.bounds.left = bounds.left;
            this.bounds.right = bounds.right;
            this.bounds.top = bounds.top;
            this.bounds.bottom = bounds.bottom;
            width = (int) (this.bounds.right - this.bounds.left);
            height = (int) (this.bounds.bottom - this.bounds.top);

            drawRect = new RectF(shadowOffset, shadowOffset, width - shadowOffset, height - shadowOffset);
            drawRect = new RectF(0, 0, width, height - shadowOffset);

            int shadowSide = feedBackShadow.getShadowSide();
            int left =  (shadowSide & FeedBackShadow.LEFT) == FeedBackShadow.LEFT ? shadowOffset : 0;
            int top = (shadowSide & FeedBackShadow.TOP) == FeedBackShadow.TOP ? shadowOffset : 0;
            int right = width - ((shadowSide & FeedBackShadow.RIGHT) == FeedBackShadow.RIGHT ? shadowOffset : 0);
            int bottom = height - ((shadowSide & FeedBackShadow.BOTTOM) == FeedBackShadow.BOTTOM ? shadowOffset : 0);

            drawRect = new RectF(left, top, right, bottom);


            invalidateSelf();

        }
    }

    private PorterDuffXfermode srcOut = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);

    @Override
    public void draw(Canvas canvas) {
        paint.setXfermode(null);

        canvas.drawRoundRect(
                drawRect,
                rx, ry,
                paint
        );

        paint.setXfermode(srcOut);
        canvas.drawRoundRect(drawRect, rx, ry, paint);
    }

    public FeedBackView setColor(int color) {
        paint.setColor(color);
        return this;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}