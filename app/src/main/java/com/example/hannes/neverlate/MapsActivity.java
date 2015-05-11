package com.example.hannes.neverlate;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

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

//new stuff

public class MapsActivity extends SlidingFragmentActivity implements View.OnClickListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng markerLocation = null;
    private LatLng gpsLocation = null;
    private TextView gpsLocationText = null;
    private TextView markerLocationText = null;
    //private TextView distanceText = null;
    private TimePicker timePicker;
    private Button setTimeButton;
    private Button insideTimePickerButton;
    private LinearLayout timeLayout;
    private int arriveTimeHours;
    private int arriveTimeMinutes;
    private boolean haveDestination = false;
    private Document doc;
    private  TempoHolder tempoHolder;
    private TextView addressText;
    private TextView arrivalTimeText;
    private TextView distanceText;
    private TextView onTimeText;
    //new stuff
    protected ListFragment mFrag;

    @Override
    //changed from protected to public
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        tempoHolder = new TempoHolder();
        gpsLocationText = (TextView) findViewById(R.id.gpsView);
        distanceText = (TextView) findViewById(R.id.distanceView);
        markerLocationText = (TextView) findViewById(R.id.markerView);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        setTimeButton = (Button) findViewById(R.id.timeButton);
        insideTimePickerButton = (Button) findViewById(R.id.insideTimePickerButton);
        setTimeButton.setOnClickListener(this);
        insideTimePickerButton.setOnClickListener(this);
        timeLayout = (LinearLayout) findViewById(R.id.timeLayout);
        timeLayout.setVisibility(View.INVISIBLE);
        addressText = (TextView) findViewById(R.id.address);
        arrivalTimeText = (TextView) findViewById(R.id.arrivalTime);
        distanceText = (TextView) findViewById(R.id.distance);
        onTimeText = (TextView) findViewById(R.id.onTime);

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
        SlidingMenu sm = getSlidingMenu();
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setMenu(R.layout.menu_layout);
        sm.setSecondaryMenu(R.layout.right_menu);

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
            //gpsLocationText.setText("GPS coord: " + loc.latitude + " , " + loc.longitude);
            gpsLocation = loc;
            if(mMap != null){
                float zoom = mMap.getCameraPosition().zoom;
                if(zoom < 10.0f) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));

                }
            }
            try {


                tempoHolder();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };
    private void tempoHolder() throws InterruptedException {

        if(haveDestination&& (arriveTimeHours+ arriveTimeMinutes) != 0 ){
            RoutePlanner routePlanner = new RoutePlanner(gpsLocation, markerLocation, RoutePlanner.MODE_WALKING);
            drawRoute(routePlanner);
            //int estimatedArrivalTime = routePlanner.getDurationValue(doc);


            //new stuff
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
            calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
            calendar.get(Calendar.MINUTE);

            int h = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            System.out.println("h: " + h + "m: " + m);
            int estimatedArrivalTime = h * 3600 + m * 60 + routePlanner.getDurationValue(doc);

            //end of new stuff

            int estimatedDistanceToTarget = routePlanner.getDistanceValue(doc);
            int timeYouWantToBeThere = (arriveTimeHours*60 + arriveTimeMinutes) * 60;
            Log.d("John","EstimatedArrivalTime: "+ estimatedArrivalTime + "TimeToBeThere:  " +timeYouWantToBeThere  );
            if(estimatedArrivalTime>timeYouWantToBeThere&&!tempoHolder.isVibrating()){
                //onTimeText.setText("NO");
                    tempoHolder = new TempoHolder();
                    tempoHolder.startVibrate(estimatedDistanceToTarget,estimatedArrivalTime
                        ,(Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE));
                //Thread t = new Thread(tempoHolder);
                //t.start();


                    tempoHolder.start();
             //tempoHolder.vibrateTheWakingSpeed(estimatedDistanceToTarget,estimatedArrivalTime
                        //,(Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE));
            }else{
                //tempoHolder = new TempoHolder();
                //onTimeText.setText("YES");
                tempoHolder.stopVibrate();
            }
    }
    }
    private GoogleMap.OnMapLongClickListener myLongClickListener = new GoogleMap.OnMapLongClickListener(){
        @Override
        public void onMapLongClick(LatLng latLng) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            markerLocation = latLng;
            markerLocationText.setText("Marker coord: " + latLng.latitude + " , " + latLng.longitude);
           if(markerLocation == null) {
               System.out.println("\n \n \n \n markerLocation is null");
           }else if(markerLocation != null){
               System.out.println("\n \n \n \n markerLocation is not null");
               RoutePlanner routePlanner = new RoutePlanner(gpsLocation, markerLocation, RoutePlanner.MODE_WALKING);
               drawRoute(routePlanner);
           }
        }
    };
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnMapLongClickListener(myLongClickListener);


    }

    private void drawRoute(RoutePlanner routePlanner){
        Log.d("John","Det fungerar" );

        System.out.println("hej");
        doc = routePlanner.getDocument();
        haveDestination = true;
        if(doc==null) {
            System.out.println("Doc null");
        }
        ArrayList<LatLng> directionPoint = routePlanner.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(3).color(
                Color.RED);

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }
        Polyline polylin = mMap.addPolyline(rectLine);
        //distanceText.setText("Distance to target: " + routePlanner.getDistanceText(doc));
        //gpsLocationText.setText(routePlanner.getArrivalTime(doc));
        //set menu labels


        //addressText.setText(routePlanner.getEndAddress(doc));
        //arrivalTimeText.setText(routePlanner.getArrivalTime(doc));
        //distanceText.setText(routePlanner.getDistanceText(doc));
    }

    @Override
    public void onClick(View v) {
        if (v == setTimeButton){

            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            vibrator.vibrate(500);
            if(timeLayout.getVisibility() == View.VISIBLE){
                timeLayout.setVisibility(View.INVISIBLE);
            }else {
                timeLayout.setVisibility(View.VISIBLE);
            }
        } else if(v == insideTimePickerButton){
            arriveTimeHours = timePicker.getCurrentHour();
            arriveTimeMinutes = timePicker.getCurrentMinute();
            timeLayout.setVisibility(View.INVISIBLE);
            markerLocationText.setText("Arrival chosen: " + arriveTimeHours + ":" + arriveTimeMinutes);

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
}
