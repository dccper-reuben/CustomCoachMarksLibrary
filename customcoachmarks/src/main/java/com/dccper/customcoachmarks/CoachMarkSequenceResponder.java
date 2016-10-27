package com.dccper.customcoachmarks;

/**
 * Created by dccperdev on 10/27/16.
 * CoachMarksSequenceResponder interface
 */

public interface CoachMarkSequenceResponder {
    void onCoachMarksWillDisplay(int position, CoachMark coachMark);
    void onCoachMarksWillDissappear(int position, CoachMark coachMark);
    void onCoachMarkSequenceDidComplete(boolean wasSkipped);
}
