package com.example.hannes.neverlate;

import android.os.Vibrator;

/**
 * Created by johnrossberg on 2015-05-11.
 */
public class TempoHolder extends  Thread {
private static double AVERAGE_STEP_LENGHT = 0.7;
   public void vibrateTheWakingSpeed(int distance, int timeToArrival, int repeatVibrations, Vibrator vibrator ) throws InterruptedException {

     double stepFromTarget = distance / AVERAGE_STEP_LENGHT;
     double timePerStep =  timeToArrival/stepFromTarget;

    for (int i=0;i<repeatVibrations;i++){

        // Vibrate for 500 milliseconds
        vibrator.vibrate(500);
        try {
            this.sleep(Math.round(timePerStep*1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   }

}
