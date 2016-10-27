package com.dccper.customcoachmarks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by dccperdev on 10/24/16.
 * Coachmarks parent viewgroup mask class
 */

public class CoachBackgroundView extends RelativeLayout {
    private Bitmap bitmap;
    private View targetView;

    public enum CutoutShape{RECTANGLE,ROUNDED_RECT,CIRCLE}

    private int statusHeight;
    private int padding = 0;
    private int maskColor = Color.BLACK;
    private int maskAlpha = 153;
    private CutoutShape selectedCutout;



    public CoachBackgroundView(Context context) {
        super(context);
    }

    public CoachBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoachBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(bitmap == null){
            createWindowFrame();
        }
        if(targetView != null){

            Canvas tempCanvas = new Canvas(bitmap);
            int locPoints[] = new int[2];
            targetView.getLocationInWindow(locPoints);

            Paint cutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cutoutPaint.setColor(Color.TRANSPARENT);
            cutoutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            switch (selectedCutout){
                case RECTANGLE:
                    RectF targetRect = new RectF(locPoints[0] + padding,locPoints[1] - statusHeight + padding,locPoints[0] + targetView.getWidth() + padding,locPoints[1] + targetView.getHeight() - statusHeight + padding);
                    tempCanvas.drawRect(targetRect,cutoutPaint);
                    break;
                case ROUNDED_RECT:
                    RectF targetRoundRect = new RectF(locPoints[0] + padding,locPoints[1] - statusHeight + padding,locPoints[0] + targetView.getWidth() + padding,locPoints[1] + targetView.getHeight() - statusHeight + padding);
                    tempCanvas.drawRoundRect(targetRoundRect,5,5,cutoutPaint);
                    break;
                case CIRCLE:
                    tempCanvas.drawCircle(locPoints[0] + (targetView.getWidth()/2),locPoints[1] + (targetView.getHeight()/2) - statusHeight,(targetView.getHeight()/2) + padding,cutoutPaint);
                    break;
            }
            canvas.drawBitmap(bitmap,0,0,null);
        }
        canvas.drawBitmap(bitmap,0,0,null);
        super.dispatchDraw(canvas);
    }

    protected void createWindowFrame(){
        bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        RectF backRect = new RectF(0,0,getWidth(),getHeight());

        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(maskColor);
        maskPaint.setAlpha(maskAlpha);
        canvas.drawRect(backRect,maskPaint);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }

    public void setTarget(View view,int statusHeight,CutoutShape targetCutout, int maskColor, int maskAlpha){
        this.targetView = view;
        this.statusHeight = statusHeight;
        this.selectedCutout = targetCutout;
        this.maskColor = maskColor;
        this.maskAlpha = maskAlpha;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }
}
