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

    @Override
    //changed from protected to public
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();


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

        notificationsThread = new Notifications((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE));
        notificationsThread.start();
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

        addressText = (TextView) rightView.findViewById(R.id.address);
        arrivalTimeText = (TextView) rightView.findViewById(R.id.arrivalTime);
        distanceText = (TextView) rightView.findViewById(R.id.distance);
        onTimeText = (TextView) rightView.findViewById(R.id.onTime);

        //Bara estetiskt
        addressText.setText(" - - - ");
        arrivalTimeText.setText(" - - - ");
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
            //gpsLocationText.setText("GPS coord: " + loc.latitude + " , " + loc.longitude);
            gpsLocation = loc;
            if(mMap != null){
                float zoom = mMap.getCameraPosition().zoom;
                if(zoom < 10.0f) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));

                }
            }
            /* Ritar upp pop-up windowet*/
            if(singleton.getArrive()){
                dialogLayout.setVisibility(View.VISIBLE);
                dialogText.setText("You have reached the destination!");
                singleton.setArrive(false);
            }
            else if(singleton.getNeedToGO()){
                dialogLayout.setVisibility(View.VISIBLE);
                dialogText.setText("Time to go!");
                singleton.setNeedToGo(false);
            }
            else if(singleton.getyouAreLate()){
                dialogLayout.setVisibility(View.VISIBLE);
                dialogText.setText("You are running late! Hurry on!");
                singleton.setYouAreLate(false);
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
                RoutePlanner routePlanner = new RoutePlanner(gpsLocation, markerLocation, transportMode);
                singleton.setRoutePlanner(routePlanner);
                drawRoute(routePlanner);

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
            singleton.setRoutePlanner(tempRoutePlaner);
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

        //distanceText.setText("Distance to target: " + routePlanner.getDistanceText(doc));
        //gpsLocationText.setText(routePlanner.getArrivalTime(doc));
        //set menu labels


        addressText.setText(routePlanner.getEndAddress(doc));
        arrivalTimeText.setText(routePlanner.getArrivalTime(doc));
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
            singleton.setTimeYouWantToBeThere(arriveTimeHours*60+arriveTimeMinutes);
            timeLayout.setVisibility(View.INVISIBLE);
            cancelNavigation.setVisibility(View.VISIBLE);
            //markerLocationText.setText("Arrival chosen: " + arriveTimeHours + ":" + arriveTimeMinutes);

        } else if(v == dialogOKButton){
            dialogLayout.setVisibility(View.INVISIBLE);
            //STOPPAR VIBRATIONERNA OM MAN TRYCKER OK P≈ DIALOGRUTAN SOM SƒGER ATT MAN ƒR AV NOTIFICATIONS
        } else if(v == cancelButton){
            singleton.setRoutePlanner(null);
            timeLayout.setVisibility(View.INVISIBLE);
        } else if(v == cancelNavigation){
            singleton.setRoutePlanner(null);
            cancelNavigation.setVisibility(View.INVISIBLE);
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
}