package com.example.hannes.neverlate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by lukasleander on 2015-05-11.
 */
public class SplashscreenActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Thread splash_screen = new Thread(){

            public void run(){
                try{
                    sleep(5000);
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                    finish();
                }
            }
        };
        splash_screen.start();
    }

}