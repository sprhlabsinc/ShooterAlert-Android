package io.greyfox.shooteralert;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.greyfox.shooteralert.app.AppConfig;
import io.greyfox.shooteralert.app.AppController;
import io.greyfox.shooteralert.app.MarkerInfoFragment;
import io.greyfox.shooteralert.app.MarkerInfoView;
import io.greyfox.shooteralert.app.ShootInfo;

import static io.greyfox.shooteralert.app.AppConfig.APP_NAME;
import static io.greyfox.shooteralert.app.AppConfig.NOTIFICATION_BROADCAST_ACTION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, View.OnClickListener {

    private static final String TAG = "MapsActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleMap mMap = null;
    private ViewPager viewPager;
    private GoogleApiClient mGoogleApiClient;
    private Marker currentMarker = null;
    private LatLng curLatLng = null;
    private Button share_but, close_but;
    private List<MarkerInfoFragment> fragments = new ArrayList<>();
    private int mPosition = 0;
    private boolean mFirst = false;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    Toast.makeText(MapsActivity.this, "New Shooting 30 miles away.", Toast.LENGTH_SHORT).show();

                    close_but.callOnClick();
                    if (AppConfig.shootInfoList.size() > 0 )
                        showSlide(AppConfig.shootInfoList.get(0));
                }
            };
            handler.postDelayed(r, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        share_but = (Button) findViewById(R.id.share_but);
        close_but = (Button) findViewById(R.id.close_but);
        share_but.setOnClickListener(this);
        close_but.setOnClickListener(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        viewPager.setPageTransformer(false, new CustPagerTransformer(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                mPosition = position;
                MarkerInfoFragment fragment = fragments.get(mPosition);
                ShootInfo mShootInfo = fragment.mShootInfo;

                LatLng place = new LatLng(mShootInfo.latitude, mShootInfo.longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 9.0f));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("shooter");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
    }

    private void showSlide(ShootInfo shootInfo) {


        for(int i = fragments.size()-1 ; i >= 0; i--){
            fragments.remove(fragments.get(i));
        }
        shootInfo.distance = 0;
        shootInfo.inside = true;
        //fragments.add(new MarkerInfoFragment(shootInfo, this));

        ShootInfo[] mShoot = new ShootInfo[AppConfig.shootInfoList.size() + 1];

        for (int i = 0; i < AppConfig.shootInfoList.size(); i ++) {
            ShootInfo temp = AppConfig.shootInfoList.get(i);

            //if (shootInfo.id == temp.id) continue;
            LatLng latLngA = new LatLng(shootInfo.latitude, shootInfo.longitude);
            LatLng latLngB = new LatLng(temp.latitude, temp.longitude);

            Location locationA = new Location("point A");
            locationA.setLatitude(latLngA.latitude);
            locationA.setLongitude(latLngA.longitude);
            Location locationB = new Location("point B");
            locationB.setLatitude(latLngB.latitude);
            locationB.setLongitude(latLngB.longitude);

            double distance = locationA.distanceTo(locationB) * 0.000621371;

            temp.inside = false;
            if (distance <= 50) {
                temp.inside = true;
            }
            temp.distance = distance;
            mShoot[i] = temp;
            //fragments.add(new MarkerInfoFragment(temp, this));
        }
        try {
            Arrays.sort(mShoot, new Comparator<ShootInfo>() {
                @Override
                public int compare(ShootInfo o1, ShootInfo o2) {

                    if (o1 == null || o2 == null) return 1;
                    if (o1.distance < o2.distance) return -1;
                    else if (o1.distance > o2.distance) return 1;
                    else return 0;
                }
            });
        } catch (Exception e) {
            int k = 0;
        }
        int j = 0;
        for (int i = 0; i < mShoot.length; i ++) {
            ShootInfo temp = mShoot[i];
            if (j >= 5 && temp.inside == false) {
                break;
            }
            fragments.add(new MarkerInfoFragment(temp, this));
            j ++;
        }

        mPosition = 0;
        LatLng place = new LatLng(shootInfo.latitude, shootInfo.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 9.0f));

        try {
            viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    MarkerInfoFragment fragment = fragments.get(position);
                    return fragment;
                }

                @Override
                public int getCount() {
                    return fragments.size();
                }
            });
        } catch (Exception e) {}

        viewPager.setVisibility(View.VISIBLE);
        share_but.setVisibility(View.VISIBLE);
        close_but.setVisibility(View.VISIBLE);
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        displayLocationSettingsRequest(getApplicationContext());
        close_but.callOnClick();
        displayShootInfos();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(LocationServices.API).build();
    }

    void displayShootInfos() {
        try {
            if (AppConfig.shootInfoList.size() == 0 || mMap == null) {
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    public void run() {
                        displayShootInfos();
                    }
                };
                handler.postDelayed(r, 100);
                return;
            }

            mMap.clear();
            if (curLatLng != null) {
                currentMarker = mMap.addMarker(new MarkerOptions().position(curLatLng).title("Current Location").
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation)));
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // Add a marker in Sydney and move the camera
            for (int i = 0; i < AppConfig.shootInfoList.size(); i++) {
                ShootInfo shootInfo = AppConfig.shootInfoList.get(i);
                LatLng shootLatLng = new LatLng(shootInfo.latitude, shootInfo.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(shootLatLng).title(String.format("%s, %s, %s", shootInfo.address, shootInfo.city, shootInfo.state)).
                        icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_marker)));
                marker.setTag(shootInfo);

                builder.include(shootLatLng);
            }
            if (!mFirst) {
                LatLngBounds bounds = builder.build();
                int padding = 0; // offset from edges of the map in pixels

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
                mFirst = true;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker.getTitle().equals("Current Location")) { return true; }

        ShootInfo shootInfo = (ShootInfo) marker.getTag();
        showSlide(shootInfo);

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = createLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;

//        curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        if (currentMarker == null) {
//            currentMarker = mMap.addMarker(new MarkerOptions().position(curLatLng).title("Current Location").
//                    icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation)));
//        }
//        else {
//            currentMarker.setPosition(curLatLng);
//        }
//        Log.d("Location Update", "Latitude: " + location.getLatitude() +
//                " Longitude: " + location.getLongitude());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Maps TAG", "Connection to Google API suspended");
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View v) {
        if (v == share_but) {
            MarkerInfoFragment fragment = fragments.get(mPosition);
            ShootInfo mShootInfo = fragment.mShootInfo;
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = String.format("Shooting at %s. Read more at %s", mShootInfo.address, mShootInfo.url2);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, APP_NAME);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        else if (v == close_but) {
            viewPager.setVisibility(View.GONE);
            share_but.setVisibility(View.GONE);
            close_but.setVisibility(View.GONE);
        }
    }

    public void moveNext() {
        if (fragments.size() > mPosition + 1) {
            viewPager.setCurrentItem(mPosition + 1);
        }
    }
}
