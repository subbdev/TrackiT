package com.subbu.trackit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.subbu.trackit.database.DatabaseAdapter;
import com.subbu.trackit.restcontroller.AppController;
import com.subbu.trackit.utils.Cache;
import com.subbu.trackit.utils.Timer;
import com.subbu.trackit.utils.Util;

import static com.subbu.trackit.utils.Util.boundsWithCenterAndLatLngDistance;
import static com.subbu.trackit.utils.Util.getCurrentRadius;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static DatabaseAdapter databaseAdapter = null;
    private GoogleMap mMap;
    public static Timer mTimer;
    float dratio;
    GoogleApiClient mGoogleApiClient;

    private Button mTrackMeButton, mSatelliteMapButton, mCurrentLocationButton, mDbServerButton,
            mCloseButton, mClearButton, mDoneButton, mSearchButton;
    private EditText mNameEditText, mCityEditText, mStateEditText, mZipEditText;
    private LinearLayout mSearchLayout;
    private Marker mLastSelectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);
        openDatabase();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTrackMeButton = (Button) findViewById(R.id.button_track_me);
        mSatelliteMapButton = (Button) findViewById(R.id.button_satellite_map_view);
        mCurrentLocationButton = (Button) findViewById(R.id.button_current_location);
        mDbServerButton = (Button) findViewById(R.id.button_db_server);

        mCloseButton = (Button) findViewById(R.id.button_close);
        mClearButton = (Button) findViewById(R.id.button_clear);
        mDoneButton = (Button) findViewById(R.id.button_done);
        mSearchButton = (Button) findViewById(R.id.button_search);

        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mCityEditText = (EditText) findViewById(R.id.edit_text_city);
        mStateEditText = (EditText) findViewById(R.id.edit_text_state);
        mZipEditText = (EditText) findViewById(R.id.edit_text_zip);

        mSearchLayout = (LinearLayout) findViewById(R.id.layout_search);

        mTrackMeButton.setOnClickListener(this);
        mSatelliteMapButton.setOnClickListener(this);
        mCurrentLocationButton.setOnClickListener(this);
        mDbServerButton.setOnClickListener(this);

        mClearButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mDoneButton.setOnClickListener(this);

        mSearchButton.setOnClickListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        float deviceWidth = displayMetrics.widthPixels;
        float deviceHeight = displayMetrics.heightPixels;
        Log.i("^^^^^^^^^^^^", displayMetrics.densityDpi + "");
        double a = displayMetrics.densityDpi * 256.0 / 160;

        double di = 256.0 * 0.0254 / displayMetrics.densityDpi;
        Log.i("^^^^^^^^^^^^", di + "");
        di = 100 * 1609.34 / di;
        double z = (Math.log10(di) - Math.log10(a)) / Math.log10(2);
        Log.i("^^^^^^^^^^^^", z + "");
        dratio = deviceHeight > deviceWidth ? deviceHeight / deviceWidth : deviceWidth / deviceHeight;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        AppController.getInstance().setMap(mMap);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location test = null;
        for (String provider : locationManager.getAllProviders()) {
            test = locationManager.getLastKnownLocation(provider);
            if (test != null)
                break;
        }
        final Location location = test;
        if (location == null) {
            Log.i("&&&&&&&&&&&", "No Location");
        }

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    LatLngBounds bounds = boundsWithCenterAndLatLngDistance(latLng, 2 * 100 * 1609.34f, 2 * 100 * 1609.34f);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                    if (Cache.getDatabaseAdapter().isTruckStopsEmpty()) {
                        AppController.getInstance().getStopPoints(latLng, latitude, longitude, 50000, MapsActivity.this);
                    }
                    Util.fromDB = true;
                }
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mLastSelectedMarker = null;
                if (Util.isManualMove) {
                    int radius = getCurrentRadius(mMap, dratio);
                    Log.i("Distance--------------", radius + "");
                    AppController.getInstance().getStopPoints(cameraPosition.target, cameraPosition.target.latitude, cameraPosition.target.longitude, radius, MapsActivity.this);
                } else {
                    Util.isManualMove = true;
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!TextUtils.isEmpty(marker.getTitle())) {
                    if (mLastSelectedMarker != null)
                        mLastSelectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(MapsActivity.this, R.drawable.truck_stop, 100, 100)));
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(MapsActivity.this, R.drawable.selected_stop, 130, 130)));
                    marker.showInfoWindow();
                    mLastSelectedMarker = marker;
                }
                return true;
            }
        });
        if (location != null) {
            onLocationChanged(location);
        }


       /* // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }


    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(this, status, 0).show();
            return false;
        }
    }

    /**
     * Open sqlite data base connections.
     */
    private void openDatabase() {
        if (Cache.getDatabaseAdapter() == null) {
            Cache.setDatabaseAdapter(new DatabaseAdapter(getApplicationContext()));
            Cache.getDatabaseAdapter().getDatabase();
        }
    }

    /**
     * Close sqlite data base connections.
     */
    private void closeDatabase() {
        Cache.getDatabaseAdapter().close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDatabase();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_track_me:

                if (mTimer == null) {
                    mTimer = new Timer(MapsActivity.this);
                    mTimer.startTimer();
                    startTracking();
                } else {
                    mTimer.stopTimer();
                    mTimer = null;
                    stopTracking();
                }
                break;

            case R.id.button_current_location:
                moveToCurrentLocation();
                break;
            case R.id.button_satellite_map_view:
                if (mSatelliteMapButton.getText().equals(getString(R.string.satellite))) {
                    mSatelliteMapButton.setText(getString(R.string.map));
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    mSatelliteMapButton.setText(getString(R.string.satellite));
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }

                break;

            case R.id.button_db_server:
                //TODO

                break;

            case R.id.button_close:
                mSearchLayout.setVisibility(View.GONE);
                break;

            case R.id.button_search:
                if (mSearchLayout.getVisibility() == View.VISIBLE) {
                    mSearchLayout.setVisibility(View.GONE);
                } else {
                    mSearchLayout.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.button_clear:

                mNameEditText.setText("");
                mCityEditText.setText("");
                mStateEditText.setText("");
                mZipEditText.setText("");
                mNameEditText.requestFocus();

                break;

            case R.id.button_done:
                String name = TextUtils.isEmpty(mNameEditText.getText()) ? "" : mNameEditText.getText().toString().trim();
                String city = TextUtils.isEmpty(mCityEditText.getText()) ? "" : mCityEditText.getText().toString().trim();
                String state = TextUtils.isEmpty(mStateEditText.getText()) ? "" : mStateEditText.getText().toString().trim();
                String zip = TextUtils.isEmpty(mZipEditText.getText()) ? "" : mZipEditText.getText().toString().trim();

                mSearchLayout.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mDoneButton.getWindowToken(), 0);

                AppController.getInstance().loadMapOnSearch(Cache.getDatabaseAdapter().getSearchTruckStops(name, city, state, zip), MapsActivity.this);

                break;
        }
    }


    private void startTracking() {
        //TODO
    }

    private void stopTracking() {
        //TODO

    }


    private void moveToCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location test = null;
        for (String provider : locationManager.getAllProviders()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            test = locationManager.getLastKnownLocation(provider);
            if (test != null)
                break;
        }

        final Location location = test;
        if (location == null) {
            Log.i("&&&&&&&&&&&", "No Location");
        } else {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            final float bearing = mMap.getCameraPosition().bearing;
            LatLng latLng = new LatLng(latitude, longitude);
            double val = (Math.log10(40075160) + Math.log10(160) + Math.log10(1080) - Math.log10(2 * 100 * 1609.34) - Math.log10(480) - Math.log10(256)) / Math.log10(2);
            Log.i("Z^^^^^^^^^^^^", val + "");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(latLng)
                    .zoom((float) val)
                    .bearing(bearing)
                    .build()));
            /*LatLngBounds bounds = boundsWithCenterAndLatLngDistance(latLng, 2 * 100 * 1609.34f, 2 * 100 * 1609.34f);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0)*//*,DURATION_IN_MS_IF_NEEDED*//*,new GoogleMap.CancelableCallback(){
                @Override
                public void onCancel() {
                    //DO SOMETHING HERE IF YOU WANT TO REACT TO A USER TOUCH WHILE ANIMATING
                }
                @Override
                public void onFinish() {
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(mMap.getCameraPosition().target)
                            .zoom(mMap.getCameraPosition().zoom)
                            .bearing(bearing)
                    .build()));
                }
            });*/
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
