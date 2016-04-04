package com.subbu.trackit.utils;

import android.os.CountDownTimer;
import android.util.Log;

import com.subbu.trackit.MapsActivity;


public class Timer {

    private static final String TAG = "Timer";
    private MapsActivity mContext;
    long milliSeconds = 15000; // 15 sec

    public Timer(MapsActivity context) {
        this.mContext = context;
    }

     CountDownTimer timer = new CountDownTimer(milliSeconds, 1000) {

         public void onTick(long millisUntilFinished) {
             //Some code
             Log.v(TAG, millisUntilFinished + "");
         }

         public void onFinish() {
             Log.v(TAG, "onFinish");
             mContext.moveToCurrentLocation();
         }
     };

     public void resetTimer() {
         timer.cancel();
         timer.start();
     }

     public void startTimer() {
         timer.start();
     }

     public void stopTimer() {
         timer.cancel();
     }

}
