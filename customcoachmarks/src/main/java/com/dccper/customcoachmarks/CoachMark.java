package com.dccper.customcoachmarks;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by dccperdev on 10/24/16.
 * Coachmarks class
 */

//@SuppressWarnings("ALL")
public class CoachMark {
    public enum Direction {BOTTOM_RIGHT, TOP_RIGHT, BOTTOM_CENTER, TOP_CENTER, BOTTOM_LEFT, TOP_LEFT}

    private Context context;
    private Window activityWindow;
    private PopupWindow popupWindow;

    private boolean allowViewTouch = false;
    private boolean allowChangeOnBackgroundTouch = true;
    private boolean allowSkipCoachMark = true;
    private boolean isCustomDirectionSpecified = false;
    private boolean isCustomizedImgResourceSet = false;
    private boolean isArrowDImenSpecified = false;
    private int width;
    private int height;
    private Direction contentLabelDirection;
    private CoachMarkPopupDelegate delegate;

    //mask customizables
    private int maskColor = Color.BLACK;
    private int maskAlpha = 153;
    private int cutoutPadding = 0;

    //skip textview customizables
    private int skipColor = Color.WHITE;
    private float skipTextSize = 20;
    private Typeface skipTypeface = Typeface.DEFAULT;

    //context textview customizables
    private StringBuilder contentText = new StringBuilder("content text");
    private int contentColor = Color.WHITE;
    private float contentTextSize = 20;
    private Typeface contentTypeface = Typeface.DEFAULT;
    private int contentMaxWidth;

    //arrow imageview customizables
    private int arrowViewResourceID;

    public CoachMark(Context context, Window activityWindow) {
        this.context = context;
        this.activityWindow = activityWindow;
    }

    public void showCoachMark(final View targetView, final CoachBackgroundView.CutoutShape targetCutoutShape) {
        /*Get target
        * Specify whether to place context above target or below
        *
        * >If target is to left of the screen use left arrows
        *  - align image to left margin add margin to center, text to right,above/below the img
        *
        * >If target is in the center region of the screen use center arrows
        *  - align image above/below to right margin add margin to center, text align to center of image
        *
        * >If target is in the right region of the screen use right arrows
        *  - align image to right margin add margin to center, text to left of img*/

        CoachBackgroundView rootView = null;
        Rect visibleRect = new Rect();
        int section;
        int targetX;
        int targetY;

        activityWindow.getDecorView().getWindowVisibleDisplayFrame(visibleRect);
        int statusHeight = visibleRect.top;
        final View view = LayoutInflater.from(context).inflate(R.layout.relative_layout_template, null);
        if (view instanceof CoachBackgroundView) {
            rootView = (CoachBackgroundView) view;
        }

        RelativeLayout groupContainer = (RelativeLayout) view.findViewById(R.id.coach_details);
        ImageView arrowImageView = (ImageView) view.findViewById(R.id.img_arrow);
        TextView contentTextView = (TextView) view.findViewById(R.id.lbl_content_view);

        RelativeLayout.LayoutParams groupContainerParams = (RelativeLayout.LayoutParams) groupContainer.getLayoutParams();
        RelativeLayout.LayoutParams contentViewParams = (RelativeLayout.LayoutParams) contentTextView.getLayoutParams();
        RelativeLayout.LayoutParams arrowImgParams = (RelativeLayout.LayoutParams) arrowImageView.getLayoutParams();

        if (rootView != null) {

            if (targetView == null) {
                //no target supplied
                //just show default context and skip
                rootView.setTarget(null, visibleRect.top, targetCutoutShape, maskColor, maskAlpha);
                rootView.setPadding(cutoutPadding);
            } else {
                rootView.setTarget(targetView, visibleRect.top, targetCutoutShape, maskColor, maskAlpha);
                rootView.setPadding(cutoutPadding);
                int third = visibleRect.width() / 3;
                int half = visibleRect.height() / 2;

                int valArr[] = new int[2];
                targetView.getLocationOnScreen(valArr);

                if (targetCutoutShape != CoachBackgroundView.CutoutShape.CIRCLE) {
                    targetX = valArr[0];
                    targetY = valArr[1] - statusHeight;
                } else {
                    targetX = valArr[0] + (targetView.getWidth() > targetView.getHeight() ? ((targetView.getWidth() - targetView.getHeight()) / 2) : (-1 * ((targetView.getWidth() - targetView.getHeight()) / 2)));
                    targetY = valArr[1] - statusHeight;
                }

                /*====SECTIONS=====
                * left top - 0
                * left bottom - 1
                * center top - 2
                * center bottom - 3
                * right top - 4
                * right bottom - 5
                *==================*/
                if (targetX > (third * 2)) {
                    //target is in the right region of the screen
                    section = targetY > half ? 5 : 4;
                } else if (targetX > third) {
                    //target is in the center region of the screen
                    section = targetY > half ? 3 : 2;
                } else {
                    //target is in the left region of the screen
                    section = targetY > half ? 1 : 0;
                }
                if (isCustomDirectionSpecified) {
                    section = contentLabelDirection.ordinal();
                }
                //add a view for the target to handle click events if required
                View targetTouchView = new View(context);
                targetTouchView.setId(View.generateViewId());
                RelativeLayout.LayoutParams targetViewParams;
                if (targetCutoutShape != CoachBackgroundView.CutoutShape.CIRCLE) {
                    targetViewParams = new RelativeLayout.LayoutParams(targetView.getWidth(), targetView.getHeight());
                } else {
                    targetViewParams = new RelativeLayout.LayoutParams(targetView.getHeight(), targetView.getHeight());
                }
                targetTouchView.setLayoutParams(targetViewParams);
                groupContainer.addView(targetTouchView);

                targetTouchView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allowViewTouch) {
                            targetView.performClick();
                        }
                    }
                });

                //content textview configs
                contentTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                contentTextView.setText(contentText);
                contentTextView.setTextColor(contentColor);
                contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, contentTextSize);
                contentTextView.setTypeface(contentTypeface);
                if (contentMaxWidth == 0) {
                    this.contentMaxWidth = 250;
                }
                contentTextView.measure(View.MeasureSpec.makeMeasureSpec(getPixelsFromDp(contentMaxWidth), View.MeasureSpec.AT_MOST), 0);
                contentTextView.setMaxWidth(getPixelsFromDp(contentMaxWidth));

                //arrow imageview configs
                arrowImageView.setVisibility(View.VISIBLE);
                if (isArrowDImenSpecified) {
                    arrowImgParams.width = getPixelsFromDp(width);
                    arrowImgParams.height = getPixelsFromDp(height);
                }
                if (isCustomizedImgResourceSet) {
                    arrowImageView.setImageResource(arrowViewResourceID);
                    arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(arrowViewResourceID), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(arrowViewResourceID), View.MeasureSpec.AT_MOST));
                }


                /*
                * For each section
                * - set the appropriate image if not customized
                * - place the targetview in the layout
                * - place the arrow according to the section
                * - place the content textview as per section
                */
                switch (section) {
                    case 0:
                        if (!isCustomizedImgResourceSet) {
                            arrowImageView.setImageResource(R.drawable.arrow_left_below);
                            arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(R.drawable.arrow_left_below), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(R.drawable.arrow_left_below), View.MeasureSpec.AT_MOST));
                        }
                        clearAllRules(groupContainerParams);
                        clearAllRules(targetViewParams);
                        clearAllRules(arrowImgParams);
                        clearAllRules(contentViewParams);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        arrowImgParams.addRule(RelativeLayout.BELOW, targetTouchView.getId());
                        arrowImgParams.addRule(RelativeLayout.ALIGN_END, targetTouchView.getId());
                        arrowImgParams.setMarginEnd(targetViewParams.width / 2);
                        contentViewParams.addRule(RelativeLayout.BELOW, arrowImageView.getId());
                        contentViewParams.addRule(RelativeLayout.RIGHT_OF, arrowImageView.getId());
                        contentViewParams.setMarginStart(-arrowImgParams.getMarginEnd());
                        groupContainerParams.leftMargin = targetX;
                        groupContainerParams.topMargin = targetY;
                        break;
                    case 1:
                        if (!isCustomizedImgResourceSet) {
                            arrowImageView.setImageResource(R.drawable.arrow_left_up);
                            arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(R.drawable.arrow_left_up), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(R.drawable.arrow_left_up), View.MeasureSpec.AT_MOST));
                        }
                        clearAllRules(groupContainerParams);
                        clearAllRules(targetViewParams);
                        clearAllRules(arrowImgParams);
                        clearAllRules(contentViewParams);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        arrowImgParams.addRule(RelativeLayout.ABOVE, targetTouchView.getId());
                        arrowImgParams.addRule(RelativeLayout.ALIGN_END, targetTouchView.getId());
                        arrowImgParams.setMarginEnd(targetViewParams.width / 2);
                        contentViewParams.addRule(RelativeLayout.ABOVE, arrowImageView.getId());
                        contentViewParams.addRule(RelativeLayout.RIGHT_OF, arrowImageView.getId());
                        contentViewParams.setMarginStart(-arrowImgParams.getMarginEnd());
                        groupContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        groupContainerParams.leftMargin = targetX;
                        groupContainerParams.bottomMargin = visibleRect.bottom - (targetY + targetView.getHeight()) - statusHeight;
                        break;
                    case 2:
                        if (!isCustomizedImgResourceSet) {
                            arrowImageView.setImageResource(R.drawable.arrow_center_below);
                            arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(R.drawable.arrow_center_below), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(R.drawable.arrow_center_below), View.MeasureSpec.AT_MOST));
                        }
                        clearAllRules(groupContainerParams);
                        clearAllRules(targetViewParams);
                        clearAllRules(arrowImgParams);
                        clearAllRules(contentViewParams);
                        targetViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        arrowImgParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        arrowImgParams.addRule(RelativeLayout.BELOW, targetTouchView.getId());
                        contentViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        contentViewParams.addRule(RelativeLayout.BELOW, arrowImageView.getId());
                        groupContainerParams.leftMargin = targetX - ((contentTextView.getMeasuredWidth() / 2) - (targetViewParams.width / 2));
                        groupContainerParams.topMargin = targetY;
                        break;
                    case 3:
                        if (!isCustomizedImgResourceSet) {
                            arrowImageView.setImageResource(R.drawable.arrow_center_up);
                            arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(R.drawable.arrow_center_up), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(R.drawable.arrow_center_up), View.MeasureSpec.AT_MOST));
                        }
                        clearAllRules(groupContainerParams);
                        clearAllRules(targetViewParams);
                        clearAllRules(arrowImgParams);
                        clearAllRules(contentViewParams);
                        targetViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        arrowImgParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        arrowImgParams.addRule(RelativeLayout.ABOVE, targetTouchView.getId());
                        contentViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        contentViewParams.addRule(RelativeLayout.ABOVE, arrowImageView.getId());
                        groupContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        groupContainerParams.leftMargin = targetX - ((contentTextView.getMeasuredWidth() / 2) - (targetViewParams.width / 2));
                        groupContainerParams.bottomMargin = visibleRect.bottom - (targetY + targetView.getHeight()) - statusHeight;
                        break;
                    case 4:
                        if (!isCustomizedImgResourceSet) {
                            arrowImageView.setImageResource(R.drawable.arrow_right_below);
                            arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(R.drawable.arrow_right_below), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(R.drawable.arrow_right_below), View.MeasureSpec.AT_MOST));
                        }
                        clearAllRules(groupContainerParams);
                        clearAllRules(targetViewParams);
                        clearAllRules(arrowImgParams);
                        clearAllRules(contentViewParams);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        arrowImgParams.addRule(RelativeLayout.BELOW, targetTouchView.getId());
                        arrowImgParams.addRule(RelativeLayout.ALIGN_START, targetTouchView.getId());
                        arrowImgParams.setMarginStart(targetViewParams.width / 2);
                        contentViewParams.addRule(RelativeLayout.BELOW, arrowImageView.getId());
                        contentViewParams.addRule(RelativeLayout.LEFT_OF, arrowImageView.getId());
                        contentViewParams.setMarginEnd(-arrowImgParams.getMarginStart());
                        groupContainerParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        groupContainerParams.rightMargin = visibleRect.right - (targetX + targetViewParams.width);
                        groupContainerParams.topMargin = targetY;
                        break;
                    case 5:
                        if (!isCustomizedImgResourceSet) {
                            arrowImageView.setImageResource(R.drawable.arrow_right_up);
                            arrowImageView.measure(View.MeasureSpec.makeMeasureSpec(getDrawableWidth(R.drawable.arrow_right_up), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getDrawableHeight(R.drawable.arrow_right_up), View.MeasureSpec.AT_MOST));
                        }
                        clearAllRules(groupContainerParams);
                        clearAllRules(targetViewParams);
                        clearAllRules(arrowImgParams);
                        clearAllRules(contentViewParams);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        targetViewParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        arrowImgParams.addRule(RelativeLayout.ABOVE, targetTouchView.getId());
                        arrowImgParams.addRule(RelativeLayout.ALIGN_START, targetTouchView.getId());
                        arrowImgParams.setMarginStart(targetViewParams.width / 2);
                        contentViewParams.addRule(RelativeLayout.ABOVE, arrowImageView.getId());
                        contentViewParams.addRule(RelativeLayout.LEFT_OF, arrowImageView.getId());
                        contentViewParams.setMarginEnd(-arrowImgParams.getMarginStart());
                        groupContainerParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        groupContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        groupContainerParams.rightMargin = visibleRect.right - (targetX + targetViewParams.width);
                        groupContainerParams.bottomMargin = visibleRect.bottom - (targetY + targetViewParams.height) - statusHeight;
                        break;
                }

                groupContainer.setLayoutParams(groupContainerParams);
                groupContainer.invalidate();
                targetTouchView.setLayoutParams(targetViewParams);
                targetTouchView.invalidate();
                arrowImageView.setLayoutParams(arrowImgParams);
                arrowImageView.invalidate();
                contentTextView.setLayoutParams(contentViewParams);
                contentTextView.invalidate();

                TextView skipTextView = (TextView) view.findViewById(R.id.lbl_skip);
                if (allowSkipCoachMark) {
                    skipTextView.setVisibility(View.VISIBLE);
                    skipTextView.setTextColor(skipColor);
                    skipTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, skipTextSize);
                    skipTextView.setTypeface(skipTypeface);
                } else {
                    skipTextView.setVisibility(View.GONE);
                }
                skipTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (delegate != null) {
                            delegate.onCoachMarkSkipped();
                        }

                        popupWindow.dismiss();
                    }
                });
                skipTextView.invalidate();
            }

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allowChangeOnBackgroundTouch) {
                        popupWindow.dismiss();
                    }
                }
            });
        }

        popupWindow = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        if (delegate != null) {
            delegate.onCoachMarkWillDisplay(this);
        }

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                showCoachMark(targetView,targetCutoutShape);
            }
        };

        try {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        } catch (WindowManager.BadTokenException e) {
            if (delegate != null) {
                delegate.rerunCoachMark();
            }
            else{
                handler.sendEmptyMessage(0);
            }
        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (delegate != null) {
                    delegate.onCoachMarkWasDismissed();
                }
            }
        });
    }

    public CoachMark setAllowViewTouch(boolean allowViewTouch) {
        this.allowViewTouch = allowViewTouch;
        return this;
    }

    public CoachMark setAllowChangeOnBackgroundTouch(boolean allowChangeOnBackgroundTouch) {
        this.allowChangeOnBackgroundTouch = allowChangeOnBackgroundTouch;
        return this;
    }

    public CoachMark setAllowSkipCoachMark(boolean allowSkipCoachMark) {
        this.allowSkipCoachMark = allowSkipCoachMark;
        return this;
    }

    public CoachMark setContentLabelDirection(Direction contentLabel) {
        this.contentLabelDirection = contentLabel;
        this.isCustomDirectionSpecified = true;
        return this;
    }

    public CoachMark setArrowImage(int drawableResourceID){
        this.arrowViewResourceID = drawableResourceID;
        this.isCustomizedImgResourceSet = true;
        return this;
    }

    public CoachMark configureMaskAttr(int color, int alpha){
        if(color != 0){
            this.maskColor = color;
        }
        if(alpha > 0 && alpha < 255){
            this.maskAlpha = alpha;
        }
        return  this;
    }

    public CoachMark configureSkipTextAttr(int color, int textSize, Typeface textTypeface){
        if(color != 0) {
            this.skipColor = color;
        }
        if(textSize !=  0){
            this.skipTextSize = textSize;
        }
        if(textTypeface != null){
            this.skipTypeface = textTypeface;
        }
        return this;
    }

    public CoachMark configureContentTextAttr(String contentText, int color, int textSize, Typeface textTypeface, int maxTextWidthInDp){
        if(!TextUtils.isEmpty(contentText)){
            this.contentText.replace(0,(contentText.length() - 1),contentText);
        }
        if (color != 0) {
            this.contentColor = color;
        }
        if(textSize != 0){
            this.contentTextSize = textSize;
        }
        if(textTypeface != null){
            this.contentTypeface = textTypeface;
        }
        if(maxTextWidthInDp > 0){
            this.contentMaxWidth = maxTextWidthInDp;
        }
        else{
            this.contentMaxWidth = 250;
        }
        return this;
    }

    public CoachMark configureArrowImage(int widthInDp, int heightInDp){
        isArrowDImenSpecified = true;
        this.width = widthInDp;
        this.height = heightInDp;
        return this;
    }

    public CoachMark addCutoutSpacing(int paddingInDp){
        this.cutoutPadding = getPixelsFromDp(paddingInDp);
        return this;
    }

    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }

    private void clearAllRules(RelativeLayout.LayoutParams params){
        for (int i = 0; i < params.getRules().length; i++) {
            params.removeRule(i);
        }
        params.setMargins(0,0,0,0);
    }

    private int getPixelsFromDp(float dpValue){
        if(context != null){
            return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,context.getResources().getDisplayMetrics()));
        }
        return 0;
    }

    private int getDrawableHeight(int resourceID){
        BitmapFactory.Options dimensions = new BitmapFactory.Options();
        dimensions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceID, dimensions);
        return dimensions.outHeight;
    }

    private int getDrawableWidth(int resourceID){
        BitmapFactory.Options dimensions = new BitmapFactory.Options();
        dimensions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceID, dimensions);
        return dimensions.outWidth;
    }

     void setCoachMarksPopupDelegate(CoachMarkPopupDelegate delegate){
        this.delegate = delegate;
    }

    interface CoachMarkPopupDelegate{
        void onCoachMarkWillDisplay(CoachMark coachMark);
        void onCoachMarkWasDismissed();
        void rerunCoachMark();
        void onCoachMarkSkipped();
    }
}