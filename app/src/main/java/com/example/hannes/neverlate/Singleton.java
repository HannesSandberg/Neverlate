package com.example.hannes.neverlate;

/**
 * Created by RossWorld on 5/13/2015.
 */
public class Singleton {
    public static  Singleton singleton = new Singleton();
    private boolean youAreLate;
    private boolean arrive;
    private boolean needToGo;
    private int timeYouWantTBoeThere = 10000000;
    private RoutePlanner routePlanner;
    public Singleton(){


    }
    public static Singleton getInstance() {
        if(singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }
    public synchronized void setYouAreLate(boolean status){
        youAreLate = status;

    }

    public synchronized void setArrive(boolean status){
        arrive = status;

    }

    public synchronized void setNeedToGo(boolean status){
        needToGo = status;

    }
    public synchronized void setTimeYouWantToBeThere(int time){
        this.timeYouWantTBoeThere = time;

    }
    public synchronized int getTimeYouWantToBeThere(){
        return this.timeYouWantTBoeThere;

    }


    public synchronized boolean getyouAreLate(){
        return youAreLate;

    }
    public synchronized boolean getArrive(){
        return arrive;

    }
    public synchronized boolean getNeedToGO(){
        return needToGo;

    }
    public synchronized RoutePlanner getRoutePlanner(){
        return  routePlanner;

    }
    public synchronized void setRoutePlanner( RoutePlanner routePlanner){
        this.routePlanner = routePlanner;

    }

}







