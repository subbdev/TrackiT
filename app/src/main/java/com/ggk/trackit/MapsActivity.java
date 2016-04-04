package com.ggk.trackit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ggk.trackit.database.DatabaseAdapter;
import com.ggk.trackit.restcontroller.AppController;
import com.ggk.trackit.utils.Cache;
import com.ggk.trackit.utils.ConnectivityUtil;
import com.ggk.trackit.utils.Timer;
import com.ggk.trackit.utils.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import static com.ggk.trackit.utils.Util.getCurrentRadius;
import static com.ggk.trackit.utils.Util.isManualMove;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final String TRACK_ME_PREFERENCES = "TrackMe";
    public static final String PREFERENCES_IS_SATELLITE_VIEW = "MapSatelliteView";
    public static final String PREFERENCES_TRACKING = "Tracking";
    private GoogleMap mMap;
    public static Timer mTimer;
    AnimationDrawable animation;

    SharedPreferences sharedpreferences;
    boolean isSatelliteView;

    private Button mSatelliteMapButton, mCloseButton, mClearButton, mDoneButton, mSearchButton;
    private ImageButton mCurrentLocationButton, mTrackMeButton;
    private TextView mTrackMeTextView;
    private EditText mNameEditText, mCityEditText, mStateEditText, mZipEditText;
    private LinearLayout mSearchLayout;
    private Marker mLastSelectedMarker;
    private int dpi;
    private int deviceWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);

        //open Databse Connection
        openDatabase();

        //initialize shared preferences
        sharedpreferences = getSharedPreferences(TRACK_ME_PREFERENCES, Context.MODE_PRIVATE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //initialize activity elements
        mTrackMeButton = (ImageButton) findViewById(R.id.button_track_me);
        mSatelliteMapButton = (Button) findViewById(R.id.button_satellite_map_view);
        mCurrentLocationButton = (ImageButton) findViewById(R.id.button_current_location);
        mTrackMeTextView = (TextView) findViewById(R.id.text_view_track_me);
        mCloseButton = (Button) findViewById(R.id.button_close);
        mClearButton = (Button) findViewById(R.id.button_clear);
        mDoneButton = (Button) findViewById(R.id.button_done);
        mSearchButton = (Button) findViewById(R.id.button_search);
        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mCityEditText = (EditText) findViewById(R.id.edit_text_city);
        mStateEditText = (EditText) findViewById(R.id.edit_text_state);
        mZipEditText = (EditText) findViewById(R.id.edit_text_zip);
        mSearchLayout = (LinearLayout) findViewById(R.id.layout_search);


        //setting listners to elements
        mTrackMeButton.setOnClickListener(this);
        mSatelliteMapButton.setOnClickListener(this);
        mCurrentLocationButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mDoneButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
        findViewById(R.id.layout_track_me).setOnClickListener(this);

        //get persisted status of tracking and maptype
        Util.isTracking = sharedpreferences.getBoolean(PREFERENCES_TRACKING, false);
        isSatelliteView = sharedpreferences.getBoolean(PREFERENCES_IS_SATELLITE_VIEW, false);


        ConnectivityUtil.check(MapsActivity.this);
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
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mSatelliteMapButton.getVisibility() == View.VISIBLE) {
                    mSearchLayout.setVisibility(View.GONE);
                    mSearchButton.setVisibility(View.GONE);
                    mSatelliteMapButton.setVisibility(View.GONE);
                    mCurrentLocationButton.setVisibility(View.GONE);
                } else {
                    mSearchButton.setVisibility(View.VISIBLE);
                    mSatelliteMapButton.setVisibility(View.VISIBLE);
                    mCurrentLocationButton.setVisibility(View.VISIBLE);
                }
            }
        });

        if (isSatelliteView) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mSatelliteMapButton.setBackground(getDrawable(R.drawable.map_view));
            mSatelliteMapButton.setText(getString(R.string.map));
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            mSatelliteMapButton.setBackground(getDrawable(R.drawable.satellite_view));
            mSatelliteMapButton.setText(getString(R.string.satellite));
        }
        if (Util.isTracking) {
            mTimer = new Timer(MapsActivity.this);
            mTimer.startTimer();
            if (animation == null) {
                animation = new AnimationDrawable();
                animation.addFrame(getResources().getDrawable(R.drawable.track_blink), 300);
                animation.addFrame(getResources().getDrawable(R.drawable.tracking), 500);
                animation.setOneShot(false);
            }
            mTrackMeButton.setBackground(animation);
            animation.start();
            mTrackMeTextView.setText(getString(R.string.tracking));
        } else {
            mTrackMeButton.setBackground(getDrawable(R.drawable.track_me));
            mTrackMeTextView.setText(getString(R.string.track_me));
            if (animation != null)
                animation.stop();
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        AppController.getInstance().setMap(mMap);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;

        dpi = displayMetrics.densityDpi;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

            return;
        }
        mMap.setMyLocationEnabled(false);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location test = null;
        for (String provider : locationManager.getAllProviders()) {
            test = locationManager.getLastKnownLocation(provider);
            if (test != null)
                break;
        }
        final Location location = test;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    moveToCurrentLocation();
                    if (Cache.getDatabaseAdapter().isTruckStopsEmpty()) {
                        AppController.getInstance().getStopPoints(null, latitude, longitude, 50000, MapsActivity.this);
                    }
                    Util.fromDB = true;
                } else {

                }
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (Util.isTracking && mTimer != null) {
                    mTimer.resetTimer();
                }
                mLastSelectedMarker = null;
                if (Util.isManualMove) {
                    int radius = getCurrentRadius(mMap, deviceWidth, dpi);

                    AppController.getInstance().getStopPoints(null, cameraPosition.target.latitude, cameraPosition.target.longitude, radius, MapsActivity.this);
                } else {
                    Util.isManualMove = true;
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!TextUtils.isEmpty(marker.getTitle())) {
                    if (mLastSelectedMarker != null)
                        mLastSelectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(MapsActivity.this, R.drawable.truck_stop_pin, 100, 100)));
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(MapsActivity.this, R.drawable.selected_stop_pin, 130, 130)));
                    marker.showInfoWindow();
                    mLastSelectedMarker = marker;
                }
                return true;
            }
        });


        if (location != null) {
            onLocationChanged(location);
        }

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (Util.isTracking && mTimer != null) {
            mTimer.resetTimer();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        moveToCurrentLocation();
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
            case R.id.layout_track_me:
                updateTrackMeStatus();
                break;
            case R.id.button_track_me:
                updateTrackMeStatus();
                break;
            case R.id.button_current_location:
                moveToCurrentLocation();
                break;
            case R.id.button_satellite_map_view:
                SharedPreferences.Editor editor = sharedpreferences.edit();

                if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    mSatelliteMapButton.setBackground(getDrawable(R.drawable.map_view));
                    mSatelliteMapButton.setText(getString(R.string.map));
                    editor.putBoolean(PREFERENCES_IS_SATELLITE_VIEW, true);
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mSatelliteMapButton.setBackground(getDrawable(R.drawable.satellite_view));
                    mSatelliteMapButton.setText(getString(R.string.satellite));
                    editor.putBoolean(PREFERENCES_IS_SATELLITE_VIEW, false);
                }
                editor.commit();

                break;


            case R.id.button_close:

                hideView(mSearchLayout);
                mSearchButton.setVisibility(View.VISIBLE);

                break;

            case R.id.button_search:

                visibleView(mSearchLayout);
                mSearchButton.setVisibility(View.GONE);
                break;

            case R.id.button_clear:

                clearSearchFields();

                break;

            case R.id.button_done:
                searchDone();

                break;
        }
    }

    private void searchDone() {
        String name = TextUtils.isEmpty(mNameEditText.getText()) ? "" : mNameEditText.getText().toString().trim();
        String city = TextUtils.isEmpty(mCityEditText.getText()) ? "" : mCityEditText.getText().toString().trim();
        String state = TextUtils.isEmpty(mStateEditText.getText()) ? "" : mStateEditText.getText().toString().trim();
        String zip = TextUtils.isEmpty(mZipEditText.getText()) ? "" : mZipEditText.getText().toString().trim();

        StringBuilder searchText = new StringBuilder();
        if (name.length() > 0) {
            searchText.append(name + ",");
        }
        if (city.length() > 0) {
            searchText.append(city + ",");
        }
        if (state.length() > 0) {
            searchText.append(state + ",");
        }
        if (zip.length() > 0) {
            searchText.append(zip + ",");
        }

        mSearchButton.setText(TextUtils.isEmpty(searchText) ? getString(R.string.search) : searchText.deleteCharAt(searchText.length() - 1));

        hideView(mSearchLayout);
        mSearchButton.setVisibility(View.VISIBLE);


        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDoneButton.getWindowToken(), 0);

        AppController.getInstance().loadMapOnSearch(Cache.getDatabaseAdapter().getSearchTruckStops(name, city, state, zip), MapsActivity.this);
    }

    private void clearSearchFields() {
        mNameEditText.setText("");
        mCityEditText.setText("");
        mStateEditText.setText("");
        mZipEditText.setText("");
        mNameEditText.requestFocus();
        mSearchButton.setText(getString(R.string.search));
    }

    private void updateTrackMeStatus() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (mTimer == null) {
            Util.isTracking = true;
            mTimer = new Timer(this);
            mTimer.startTimer();
            mTrackMeTextView.setText(getString(R.string.tracking));
            if (animation == null) {
                animation = new AnimationDrawable();
                animation.addFrame(getResources().getDrawable(R.drawable.track_blink), 300);
                animation.addFrame(getResources().getDrawable(R.drawable.tracking), 500);
                animation.setOneShot(false);
            }
            mTrackMeButton.setBackground(animation);
            animation.start();
            editor.putBoolean(PREFERENCES_TRACKING, true);
        } else {
            Util.isTracking = false;
            mTimer.stopTimer();
            mTimer = null;
            mTrackMeTextView.setText(getString(R.string.track_me));
            mTrackMeButton.setBackground(getDrawable(R.drawable.track_me));
            editor.putBoolean(PREFERENCES_TRACKING, false);
            if (animation != null)
                animation.stop();
        }
        editor.commit();
    }

    private void hideView(final View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_out);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
        });

        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDoneButton.getWindowToken(), 0);
        view.startAnimation(animation);
    }

    private void visibleView(final View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
        });

        view.startAnimation(animation);
    }


    public void moveToCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location test = null;
        for (String provider : locationManager.getAllProviders()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

                return;
            }
            test = locationManager.getLastKnownLocation(provider);
            if (test != null)
                break;
        }

        final Location location = test;
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            final float bearing = mMap.getCameraPosition().bearing;
            LatLng latLng = new LatLng(latitude, longitude);
            double val = (Math.log10(40075160) + Math.log10(160) + Math.log10(deviceWidth) - Math.log10(2 * 100 * 1609.34) - Math.log10(dpi) - Math.log10(256)) / Math.log10(2);

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(latLng)
                    .zoom((float) val)
                    .bearing(bearing)
                    .build()));
            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(100 * 1609.34)
                    .strokeColor(Color.GREEN)
                    .fillColor(0x1500ff00));

            int radius = getCurrentRadius(mMap, deviceWidth, dpi);

            AppController.getInstance().getStopPoints(latLng, latitude, longitude, radius, MapsActivity.this);
            isManualMove = false;

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
