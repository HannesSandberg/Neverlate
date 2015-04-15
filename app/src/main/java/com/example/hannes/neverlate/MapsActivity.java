package com.example.hannes.neverlate;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng markerLocation = null;
    private LatLng gpsLocation = null;
    private TextView gpsLocationText = null;
    private TextView markerLocationText = null;
    private TextView distanceText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        /**
         *  Note Michal Stypa:
         *  Strict mode disables the ability to connect to internet on main thread in order to prevent accidental
         *  network access. Networking should be handled by separate threads to prevent program crashes on network failure.
         *  Following two rows shall be removed when network connection is moved to separate thread!!
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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
            gpsLocationText = (TextView) findViewById(R.id.gpsView);
            gpsLocationText.setText("GPS coord: " + loc.latitude + " , " + loc.longitude);
            gpsLocation = loc;
            if(mMap != null){
                float zoom = mMap.getCameraPosition().zoom;
                if(zoom < 10.0f) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                }
            }
        }
    };
    private GoogleMap.OnMapLongClickListener myLongClickListener = new GoogleMap.OnMapLongClickListener(){
        @Override
        public void onMapLongClick(LatLng latLng) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            markerLocation = latLng;
            markerLocationText = (TextView) findViewById(R.id.markerView);
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
        Document doc = routePlanner.getDocument();

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
        distanceText = (TextView) findViewById(R.id.distanceView);
        distanceText.setText("Distance to target: " + routePlanner.getDistanceText(doc));
    }

  /*  private void oldSetUpMap{
        GPSTracker tracker = new GPSTracker(MapsActivity.this);
        double latitude = tracker.getLatitude();
        double longitude = tracker.getLongitude();
        TextView tv = (TextView) findViewById(R.id.gpsView);
        tv.setText(latitude + " , " + longitude);
        // mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude )).title("Marker"));
       *//* Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(10000)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));*//*
        mMap.setMyLocationEnabled(true);
    }*/
}
