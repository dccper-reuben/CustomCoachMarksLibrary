package com.dccper.customcoachmarks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dccperdev on 10/24/16.
 * Coachmarks sequence class
 */

@SuppressWarnings("unused")
public class CoachMarksSequence implements CoachMarkPopupDelegate,CoachMarkPopupErrorHandlerDelegate{
    private int currentItem = 0;
    private boolean wasSkipped = false;
    private Context context;
    private List<CoachMark> coachMarkList = new ArrayList<>();
    private List<View> targetViewList = new ArrayList<>();
    private List<CoachBackgroundView.CutoutShape> targetCutoutList = new ArrayList<>();

    private CoachMarkSequenceResponder delegate;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(currentItem < coachMarkList.size()){
                coachMarkList.get(currentItem).showCoachMark((targetViewList.get(currentItem).getTag() == null?targetViewList.get(currentItem):null),targetCutoutList.get(currentItem));
            }
        }
    };

    public CoachMarksSequence(Context context) {
        this.context = context;
    }

    public CoachMarksSequence addToSequence(@NonNull CoachMark coachMarkObj, View targetView, CoachBackgroundView.CutoutShape shape){
        coachMarkObj.setCoachMarksPopupDelegate(this);
        coachMarkObj.setErrorHandlerDelegate(this);
        coachMarkList.add(coachMarkObj);
        if(targetView != null){
            this.targetViewList.add(targetView);
        }
        else{
            //null entered
            View dummyView = new View(context);
            dummyView.setTag(1);
            this.targetViewList.add(dummyView);
        }
        if(shape != null){
            this.targetCutoutList.add(shape);
        }
        else {
            //null entered
            this.targetCutoutList.add(CoachBackgroundView.CutoutShape.RECTANGLE);
        }
        return this;
    }

    public void start(){
        if(currentItem < coachMarkList.size()){
            coachMarkList.get(currentItem).showCoachMark((targetViewList.get(currentItem).getTag() == null?targetViewList.get(currentItem):null),targetCutoutList.get(currentItem));
        }
    }

    public void setDelegate(CoachMarkSequenceResponder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onCoachMarkWasDismissed() {
        if(delegate != null){
            delegate.onCoachMarksWillDissappear(currentItem, coachMarkList.get(currentItem));
        }
        currentItem++;
        if(currentItem < coachMarkList.size()){
            coachMarkList.get(currentItem).showCoachMark((targetViewList.get(currentItem).getTag() == null?targetViewList.get(currentItem):null),targetCutoutList.get(currentItem));
        }
        else {
            if(delegate != null){
                delegate.onCoachMarkSequenceDidComplete(wasSkipped);
            }
        }
    }

    @Override
    public void rerunCoachMark() {
        handler.sendEmptyMessage(0);
    }

    @Override
    public void onCoachMarkWillDisplay(CoachMark coachMark) {
        if(delegate != null){
            delegate.onCoachMarksWillDisplay(currentItem, coachMark);
        }
    }

    @Override
    public void onCoachMarkSkipped() {
        currentItem = coachMarkList.size();
        wasSkipped = true;
    }
}
