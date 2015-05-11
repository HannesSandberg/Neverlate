package com.example.hannes.neverlate;

import android.os.Vibrator;
import android.util.Log;

/**
 * Created by johnrossberg on 2015-05-11.
 */
public  class TempoHolder extends  Thread {
private static double AVERAGE_STEP_LENGHT = 0.7;
private static int REPAEAT_VIBRATIONS = 5;
private boolean isVibrating = false;
private int timeToArrival;
    private int distance;
    private Vibrator vibrator;


   public boolean isVibrating(){
        return isVibrating;
    }

    public void startVibrate(int timeToArrival, int distance, Vibrator vibrator){
        isVibrating = true;
        this.timeToArrival = timeToArrival;
        this.distance = distance;
        this.vibrator = vibrator;

    }
    public void stopVibrate(){
        isVibrating = false;
    }
    public void run(){
        while(isVibrating){
            try {
                vibrateTheWakingSpeed(distance, timeToArrival,vibrator);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

   public void vibrateTheWakingSpeed(int distance, int timeToArrival, Vibrator vibrator ) throws InterruptedException {

     double stepFromTarget = distance / AVERAGE_STEP_LENGHT;
     double timePerStep =  timeToArrival/stepFromTarget;
       //isVibrating = true;
    for (int i=0;i<REPAEAT_VIBRATIONS;i++){
        Log.d("John", "Runs the for loop in TempeHolder");
        // Vibrate for 500 milliseconds
        vibrator.vibrate(500);
        try {
            Log.d("John", "timePerStep " + timePerStep);
            this.sleep(Math.round(timePerStep*1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
       //isVibrating= false;

   }

}
