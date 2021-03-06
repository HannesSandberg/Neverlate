package com.example.hannes.neverlate;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MapsActivity extends SlidingFragmentActivity implements View.OnClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng markerLocation = null;
    private LatLng gpsLocation = null;
    private TextView gpsLocationText = null;
    private TextView markerLocationText = null;
    //private TextView distanceText = null;
    private TimePicker timePicker;
    private ImageButton menuButton;
    private Button insideTimePickerButton;
    private LinearLayout timeLayout;
    private RelativeLayout dialogLayout;
    private int arriveTimeHours;
    private int arriveTimeMinutes;
    private boolean haveDestination = false;
    private Document doc;
   // private TempoHolder tempoHolder;
    private TextView addressText;
    private TextView arrivalTimeText;
    private TextView distanceText;
    private TextView onTimeText;
    //new stuff
    protected ListFragment mFrag;
    private SlidingMenu sm;
    private TextView dialogText;
    private Button dialogOKButton;
    private Singleton singleton;
    private ToggleButton toggle;
    private String transportMode;
    private Notifications notificationsThread;
    private Button cancelButton;
    private ImageButton cancelNavigation;
    private ImageButton more;
    private RoutePlanner rp;
    private TextView estimatedTimeText;
    private int tempTime = 0;
    private MediaPlayer mp1, mp2, mp3;



    @Override
    //changed from protected to public
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        //tempoHolder = new TempoHolder();


        //Setting default transport mode to walking
        transportMode = RoutePlanner.MODE_WALKING;

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        insideTimePickerButton = (Button) findViewById(R.id.insideTimePickerButton);
        menuButton.setOnClickListener(this);
        insideTimePickerButton.setOnClickListener(this);
        timeLayout = (LinearLayout) findViewById(R.id.timeLayout);
        timeLayout.setVisibility(View.INVISIBLE);
        dialogLayout = (RelativeLayout) findViewById(R.id.dialogLayout);
        dialogLayout.setVisibility(View.INVISIBLE);
        menuButton.getBackground().setAlpha(210);
        dialogText = (TextView) findViewById(R.id.dialogText);
        dialogOKButton = (Button) findViewById(R.id.insideDialogButton);
        dialogOKButton.setOnClickListener(this);
        this.singleton = Singleton.getInstance();
        cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);
        cancelNavigation = (ImageButton) findViewById(R.id.cancelNavigation);
        cancelNavigation.setOnClickListener(this);
        cancelNavigation.setVisibility(View.INVISIBLE);
        cancelNavigation.getBackground().setAlpha(210);
        more = (ImageButton) findViewById(R.id.more);
        more.getBackground().setAlpha(210);
        more.setOnClickListener(this);



        //Startar trÂden.
        //new Notifications((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE)).run();

        notificationsThread = new Notifications((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE));
        notificationsThread.start();

        mp1 = MediaPlayer.create(getApplicationContext(), R.raw.leave );
        mp2 = MediaPlayer.create(getApplicationContext(), R.raw.reached );
        mp3 = MediaPlayer.create(getApplicationContext(), R.raw.you_are_late );



        /**
         *  Note Michal Stypa:
         *  Strict mode disables the ability to connect to internet on main thread in order to prevent accidental
         *  network access. Networking should be handled by separate threads to prevent program crashes on network failure.
         *  Following two rows shall be removed when network connection is moved to separate thread!!
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //SlidingMenu stuff

        setBehindContentView(R.layout.menu_layout);
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        mFrag = new MenuList();
        ft.replace(R.id.list_placeholder, mFrag);
        ft.commit();
        sm = getSlidingMenu();
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setMenu(R.layout.menu_layout);
        sm.setSecondaryMenu(R.layout.right_menu);
        View rightView = sm.getSecondaryMenu();
        toggle = (ToggleButton) sm.getMenu().findViewById(R.id.toggle); //Toggle button
        toggle.setOnClickListener(toggleClickListener); //Add action listener

        arrivalTimeText = (TextView) rightView.findViewById(R.id.arrivalTime);
        addressText = (TextView) rightView.findViewById(R.id.address);
        estimatedTimeText = (TextView) rightView.findViewById(R.id.chosenTimeText);
        distanceText = (TextView) rightView.findViewById(R.id.distance);
        onTimeText = (TextView) rightView.findViewById(R.id.onTime);

        //Bara estetiskt
        addressText.setText(" - - - ");
        estimatedTimeText.setText(" - - - ");
        distanceText.setText(" - - - ");
        onTimeText.setText(" - - - ");




        sm.setShadowWidth(15);
        sm.setBehindOffset(300); // Pixels from right screen edge to right menu edge
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        sm.setShadowDrawable(R.drawable.shadow);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            gpsLocation = loc;
            if(mMap != null){
                float zoom = mMap.getCameraPosition().zoom;
                if(zoom < 10.0f) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));

                }
            }
            //Draw new route
            if(markerLocation != null){
            rp = new RoutePlanner(gpsLocation, markerLocation,transportMode);
            drawRoute(rp);
            }

            /* Ritar upp pop-up windowet*/
           Calendar calendar = GregorianCalendar.getInstance();
           int currTimeS = calendar.get(Calendar.SECOND);
           int currTimeM =  calendar.get(Calendar.MINUTE);
           int currTimeH = calendar.get(Calendar.HOUR);
           int currTime = currTimeM*60 +currTimeS + currTimeH*3600;
           int time = currTime - tempTime ;

            if(time >20&&singleton.getRoutePlanner()!=null) {
                if (singleton.getArrive()) {
                    dialogLayout.setVisibility(View.VISIBLE);
                    dialogText.setText("You have reached the destination!");
                    singleton.setArrive(false);
                    singleton.setYouAreLate(false);
                    singleton.setNeedToGo(false);
                    tempTime = currTime;
                    mp2.start();
                } else if (singleton.getNeedToGO()) {
                    dialogLayout.setVisibility(View.VISIBLE);
                    dialogText.setText("Time to go!");
                    singleton.setArrive(false);
                    singleton.setYouAreLate(false);
                    singleton.setNeedToGo(false);
                    tempTime = currTime;
                    mp1.start();
                } else if (singleton.getyouAreLate()) {
                    dialogLayout.setVisibility(View.VISIBLE);
                    dialogText.setText("You are running late! Hurry on!");
                    singleton.setArrive(false);
                    singleton.setYouAreLate(false);
                    singleton.setNeedToGo(false);
                    tempTime = currTime;
                    mp3.start();
                }
            }

            if(singleton.getyouAreLate()){
                onTimeText.setText("NO");
            }else{
                onTimeText.setText("YES");
            }

            if(doc != null) {
                addressText.setText(rp.getEndAddress(doc));
                estimatedTimeText.setText(rp.getArrivalTime(doc));
                distanceText.setText(rp.getDistanceText(doc));
            }

        }
    };

    private GoogleMap.OnMapLongClickListener myLongClickListener = new GoogleMap.OnMapLongClickListener(){
        @Override
        public void onMapLongClick(LatLng latLng) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            markerLocation = latLng;

            if(markerLocation == null) {
                System.out.println("\n \n \n \n markerLocation is null");
            }else if(markerLocation != null){
                System.out.println("\n \n \n \n markerLocation is not null");
                rp = new RoutePlanner(gpsLocation, markerLocation, transportMode);
                singleton.setRoutePlanner(new RoutePlanner(gpsLocation, markerLocation, transportMode));
                drawRoute(rp);

            }

            timeLayout.setVisibility(View.VISIBLE);
        }
    };

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnMapLongClickListener(myLongClickListener);


    }
    private ToggleButton.OnClickListener toggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changeTransportMode();
        }
    };


    private void changeTransportMode(){
        if(transportMode == RoutePlanner.MODE_WALKING){
            transportMode = RoutePlanner.MODE_BIKING;
        }else if(transportMode == RoutePlanner.MODE_BIKING){
            transportMode = RoutePlanner.MODE_WALKING;
        }
        if(singleton.getRoutePlanner() != null){
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(markerLocation));
            RoutePlanner tempRoutePlaner = new RoutePlanner(gpsLocation, markerLocation, transportMode);
            rp = tempRoutePlaner;
            singleton.setRoutePlanner(new RoutePlanner(gpsLocation, markerLocation, transportMode));
            drawRoute(tempRoutePlaner);
        }

    }

    private void drawRoute(RoutePlanner routePlanner){
        doc = routePlanner.getDocument();
        haveDestination = true;
        if(doc==null) {
            System.out.println("Doc null");
        }
        ArrayList<LatLng> directionPoint = routePlanner.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(5).color(
                Color.RED);
        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }
        Polyline polylin = mMap.addPolyline(rectLine);

        addressText.setText(routePlanner.getEndAddress(doc));
        estimatedTimeText.setText(routePlanner.getArrivalTime(doc));
        distanceText.setText(routePlanner.getDistanceText(doc));
    }

    @Override
    public void onClick(View v) {
        if (v == menuButton){
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    sm.showMenu();
                }
            });

        } else if(v == insideTimePickerButton){
            arriveTimeHours = timePicker.getCurrentHour();
            arriveTimeMinutes = timePicker.getCurrentMinute();
            singleton.setTimeYouWantToBeThere(arriveTimeHours*3600+arriveTimeMinutes*60);
            timeLayout.setVisibility(View.INVISIBLE);
            cancelNavigation.setVisibility(View.VISIBLE);
            arrivalTimeText.setText(arriveTimeHours + ":" + arriveTimeMinutes);
            Calendar calendar = GregorianCalendar.getInstance();
            timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        } else if(v == dialogOKButton){
            dialogLayout.setVisibility(View.INVISIBLE);
            //STOPPAR VIBRATIONERNA OM MAN TRYCKER OK P≈ DIALOGRUTAN SOM SƒGER ATT MAN ƒR SEN
            if(dialogText.getText().toString().contains("Time to go")) {
                singleton.sleepNotification(30000);
                singleton.setYouAreLate(false);
                singleton.setNeedToGo(false);
            }

            else if(dialogText.getText().toString().contains("You are running late")){
                    singleton.sleepNotification(30000);
                    singleton.setYouAreLate(false);
                    singleton.setNeedToGo(false);
            }
        } else if(v == cancelButton){
            singleton.setRoutePlanner(null);
            timeLayout.setVisibility(View.INVISIBLE);
            mMap.clear();
        } else if(v == cancelNavigation){
            singleton.setRoutePlanner(null);
            cancelNavigation.setVisibility(View.INVISIBLE);
            mMap.clear();
        } else if(v == more){
            sm.showSecondaryMenu();
        }
        return;
    }

    //new stuff
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                toggle();
                return true;
        }
        return onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    public class BasePagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments = new ArrayList<Fragment>();
        private ViewPager mPager;

        public BasePagerAdapter(FragmentManager fm, ViewPager vp){
            super(fm);
            mPager = vp;
            mPager.setAdapter(this);
            for (int i = 0; i < 3; i++){
                addTab(new MenuList());
            }
        }

        public void addTab(Fragment frag){
            mFragments.add(frag);
        }

        @Override
        public Fragment getItem(int position){
            return mFragments.get(position);
        }

        @Override
        public int getCount(){
            return mFragments.size();
        }
    }

    private String checkZero(String time){
        //Kolla nollor
        if(time.length() == 3){

        }
        return time;
    }
}