package com.example.hannes.neverlate;

import android.os.Vibrator;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by RossWorld on 5/13/2015.
 */
public class Notifications extends Thread {
    private RoutePlanner routePlanner;
    private Document doc;
    private Singleton singleton;
    private Vibrator vibrator;
    private final static int DISTANCE_UNTIL_ARRIVAL = 30;
    public  Notifications( Vibrator vibrator)  {

        this.vibrator= vibrator;
        this.singleton = Singleton.getInstance();
    }

    public void run() {
        int timeYouWantTOBeThere = 0;

        ArrayList<Integer> vibrationTimeList = getVibrationTimeList();
        while (true) {
            routePlanner = singleton.getRoutePlanner();
            if (routePlanner != null) {
                this.doc = routePlanner.getDocument();

                // kollar om man ‰r framme
                if (routePlanner.getDistanceValue(doc) < DISTANCE_UNTIL_ARRIVAL) {
                    singleton.setArrive(true);
                    //Tar bort routePlannerna ner vi kommer fram. So att man bara kommer fram en gong po en plan
                    singleton.setRoutePlanner(null);
                } else {

                    if (this.getEstimatedArrivalTime() > singleton.getTimeYouWantToBeThere()) {
                        if (timeYouWantTOBeThere != singleton.getTimeYouWantToBeThere()) {
                            timeYouWantTOBeThere = singleton.getTimeYouWantToBeThere();
                            singleton.setNeedToGo(true);

                        } else {
                            singleton.setYouAreLate(true);
                        }
                        //vibrerar
                        int i = 1;
                        while (i < vibrationTimeList.size()) {
                            vibrator.vibrate(vibrationTimeList.get(i));
                            i++;
                            try {

                                this.sleep(vibrationTimeList.get(i));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // slut po vibrartionskoden.


                    } else {
                        try {
                            this.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            } else {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private int getEstimatedArrivalTime(){


        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        calendar.get(Calendar.MINUTE);

        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int estimatedArrivalTime = h * 3600 + m * 60 + routePlanner.getDurationValue(doc);

        return estimatedArrivalTime;
    }

    /* H‰r best‰mer man hur lÂng tid den ska vibrerar/sova. */
    private ArrayList<Integer> getVibrationTimeList(){
        ArrayList<Integer> list = new ArrayList<Integer>();
        //vibrerar i 500ms
        list.add(500);
        //sover i 10000ms
        list.add(1000);

        //vibrerar i 700ms
        list.add(700);
        //sover i 700ms
        list.add(700);

        list.add(500);
        list.add(450);

        return list;


    }

}
