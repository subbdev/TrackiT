package com.subbu.trackit.utils;

import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;

import com.subbu.trackit.MapsActivity;


public class Timer {

    private static final String TAG = "Timer";
    private Activity mContext;
    long milliSeconds = 900000; // 15 minutes

    public Timer(Activity context){
        this.mContext = context;
    }

     CountDownTimer timer = new CountDownTimer(milliSeconds, 1000) {

         public void onTick(long millisUntilFinished) {
             //Some code
             Log.v(TAG, millisUntilFinished+"");
         }

         public void onFinish() {
             Log.v(TAG, "onFinish");
             MapsActivity.mTimer = null;
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
