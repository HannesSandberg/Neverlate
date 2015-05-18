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
    private final static int TIME_NOTIFICATIONS_SLEEPS = 30000;
    public  Notifications( Vibrator vibrator)  {

        this.vibrator= vibrator;
        this.singleton = Singleton.getInstance();
    }

    public void run() {
        int timeYouWantTOBeThere = 0;

        ArrayList<Integer> vibrationTimeList = getVibrationTimeList();
        while (true) {

            if (singleton.getSleepNotifivationTime() == 0) {
                routePlanner = singleton.getRoutePlanner();
                if (routePlanner != null) {
                    this.doc = routePlanner.getDocument();

                    // kollar om man ‰r framme
                    if (doc != null) {
                        if (routePlanner.getDistanceValue(doc) < DISTANCE_UNTIL_ARRIVAL) {
                            singleton.setArrive(true);
                            //Tar bort routePlannerna ner vi kommer fram. So att man bara kommer fram en gong po en plan
                            singleton.setRoutePlanner(null);
                        } else {
                                    System.out.println("getEstimatedArrivalTime() "+this.getEstimatedArrivalTime()+ " getTimeYouWantToBeThere "+
                                            singleton.getTimeYouWantToBeThere());
                            if (this.getEstimatedArrivalTime()+ 60 > singleton.getTimeYouWantToBeThere()) {
                                if (timeYouWantTOBeThere != singleton.getTimeYouWantToBeThere()) {
                                    timeYouWantTOBeThere = singleton.getTimeYouWantToBeThere();
                                    singleton.setNeedToGo(true);

                                } else if(this.getEstimatedArrivalTime() > singleton.getTimeYouWantToBeThere()) {
                                    singleton.setYouAreLate(true);

                                    //vibrerar
                                    int i = 1;
                                    while (i < vibrationTimeList.size()) {
                                      //  vibrator.vibrate(vibrationTimeList.get(i - 1));
                                        vibrator.vibrate(400);
                                        i++;
                                        try {


                                        //    this.sleep(vibrationTimeList.get(i - 1));
                                            this.sleep(300);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        i++;
                                    }
                                    // slut po vibrartionskoden.
                                }

                            } else {
                                try {
                                    this.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }

                } else {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else{
            try {
                sleep(singleton.getSleepNotifivationTime());
                singleton.sleepNotification(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        }
    }
    private int getEstimatedArrivalTime(){



        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance


        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int estimatedArrivalTime = h * 3600 + m * 60 + routePlanner.getDurationValue(doc);

        return estimatedArrivalTime;
    }

    /* H‰r best‰mer man hur lÂng tid den ska vibrerar/sova. */
    private ArrayList<Integer> getVibrationTimeList(){
        ArrayList<Integer> list = new ArrayList<Integer>();

        list.add(400);
        list.add(700);




        return list;


    }

}
