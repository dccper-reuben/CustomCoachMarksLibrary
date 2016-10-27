package com.dccper.customcoachmarks;

/**
 * Created by dccperdev on 10/27/16.
 * CoachMarksPopupDelegate interface
 */

public interface CoachMarkPopupDelegate {
    void onCoachMarkWillDisplay(CoachMark coachMark);
    void onCoachMarkWasDismissed();
    void onCoachMarkSkipped();
}
