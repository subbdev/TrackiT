package com.ggk.transflo.restcontroller;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ggk.transflo.CustomInfoWindowAdapter;
import com.ggk.transflo.R;
import com.ggk.transflo.beans.ResBean;
import com.ggk.transflo.beans.TruckStop;
import com.ggk.transflo.utils.Cache;
import com.ggk.transflo.utils.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppController extends Application {
    private GoogleMap map;
    public static final String TAG = AppController.class
            .getSimpleName();
    final String REQ_TAG = "json_obj_req";
    String BASE_URL = "http://webapp.transflodev.com/svc1.transflomobile.com/api/v3/stations/";
    final String BASE_AUTH = "Basic amNhdGFsYW5AdHJhbnNmbG8uY29tOnJMVGR6WmdVTVBYbytNaUp6RlIxTStjNmI1VUI4MnFYcEVKQzlhVnFWOEF5bUhaQzdIcjVZc3lUMitPTS9paU8=";
    JSONObject requestBody = new JSONObject();
    Gson gson = new Gson();
    private RequestQueue mRequestQueue;
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    public void getStopPoints(final LatLng latLng, final double lat, final double lng, final int radius, final Activity activity) {

        try {
            requestBody.put("lat", lat);
            requestBody.put("lng", lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final int currentValue = ++Util.currentCall;

        if (Util.fromDB && radius != 50000) {
            ArrayList<TruckStop> lst = Cache.getDatabaseAdapter().getTruckStopsByLocNRad(lat, lng, radius);
            map.clear();

            MarkerOptions marker = null;
            if (latLng != null) {
                marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(activity, R.drawable.current_location_pin, 100, 100))).position(latLng);
                map.addMarker(marker);

                map.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(100 * 1609.34)
                        .strokeColor(Color.GREEN)
                        .fillColor(0x1500ff00));
            }
            if (lst.size() > 0) {

                if (Util.marker_icon == null)
                    Util.marker_icon = BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(activity, R.drawable.truck_stop_pin, 100, 100));
                for (TruckStop stop : lst) {
                    map.setInfoWindowAdapter(new CustomInfoWindowAdapter(activity));
                    marker = new MarkerOptions()
                            .icon(Util.marker_icon)
                            .position(new LatLng(stop.getLat(), stop.getLng()));
                    marker.title(stop.getName());

                    String snippet = getSnippet(lat, lng, stop);

                    marker.snippet(snippet);

                    map.addMarker(marker);
                }
            }
        } else {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    BASE_URL + radius, requestBody,
                    new Response.Listener<JSONObject>() {
                        public Bitmap resizeMapIcons(String iconName, int width, int height) {
                            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
                            return resizedBitmap;
                        }

                        @Override
                        public void onResponse(JSONObject response) {

                            if (currentValue == Util.currentCall || radius == 50000) {
                                ResBean res = gson.fromJson(response.toString(), ResBean.class);

                                map.clear();
                                MarkerOptions marker = null;
                                if (latLng != null) {
                                    marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(activity, R.drawable.current_location_pin, 100, 100))).position(latLng);
                                    map.addMarker(marker);

                                    map.addCircle(new CircleOptions()
                                            .center(latLng)
                                            .radius(100 * 1609.34)
                                            .strokeColor(Color.GREEN)
                                            .fillColor(0x1500ff00));
                                }
                                if (res.getTruckStops().size() > 0) {


                                    if (Util.marker_icon == null)
                                        Util.marker_icon = BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(activity, R.drawable.truck_stop_pin, 100, 100));
                                    for (TruckStop stop : res.getTruckStops()) {
                                        if (radius != 50000) {
                                            map.setInfoWindowAdapter(new CustomInfoWindowAdapter(activity));
                                            marker = new MarkerOptions()
                                                    .icon(Util.marker_icon)
                                                    .position(new LatLng(stop.getLat(), stop.getLng()));
                                            marker.title(stop.getName());
                                            String snippet = getSnippet(lat, lng, stop);
                                            marker.snippet(snippet);
                                            map.addMarker(marker);
                                        }
                                        if (radius == 50000)
                                            Cache.getDatabaseAdapter().insertTruckStops(stop);
                                    }
                                }
                            }
                        }


                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", BASE_AUTH);
                    return headers;
                }
            };
            this.cancelPendingRequests(REQ_TAG);
            this.addToRequestQueue(jsonObjReq, REQ_TAG);
        }
    }

    @NonNull
    private String getSnippet(double lat, double lng, TruckStop stop) {
        float[] distance = new float[1];
        Location.distanceBetween(lat, lng, stop.getLat(), stop.getLng(), distance);

        StringBuilder snippet = getSnippetStringBuilder(stop, distance[0]);
        return snippet.toString();
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public void loadMapOnSearch(ArrayList<TruckStop> truckStops, Activity activity) {

        map.clear();
        if (truckStops.size() > 0) {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = null;
            for (String provider : locationManager.getAllProviders()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                location = locationManager.getLastKnownLocation(provider);
                if (location != null)
                    break;
            }


            if (Util.marker_icon == null)
                Util.marker_icon = BitmapDescriptorFactory.fromBitmap(Util.resizeMapIcons(activity, R.drawable.truck_stop_pin, 100, 100));
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (TruckStop stop : truckStops) {
                LatLng point = new LatLng(stop.getLat(), stop.getLng());
                map.setInfoWindowAdapter(new CustomInfoWindowAdapter(activity));
                MarkerOptions marker = new MarkerOptions()
                        .icon(Util.marker_icon)
                        .position(point);
                builder.include(point);

                marker.title(stop.getName());

                float distance = 0;

                if (location != null) {
                    float[] distanceArray = new float[1];
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(), stop.getLat(), stop.getLng(), distanceArray);
                    distance = distanceArray[0];
                }

                StringBuilder snippet = getSnippetStringBuilder(stop, distance);
                marker.snippet(snippet.toString());
                map.addMarker(marker);
            }
            Util.isManualMove = false;
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
        }
    }

    @NonNull
    private StringBuilder getSnippetStringBuilder(TruckStop stop, float distance) {
        StringBuilder snippet = new StringBuilder();
        snippet.append("<html><body> Distance : " + distance * 0.000621371 + " miles" +
                "<br/>City : " + stop.getCity() +
                "<br/>State : " + stop.getState() +
                "<br/>Country : " + stop.getCountry() +
                "<br/> Zip : " + stop.getZip());
        if (!TextUtils.isEmpty(stop.getRawLine1())) {
            snippet.append("<br/> RawLine1 : " + stop.getRawLine1());
        }
        if (!TextUtils.isEmpty(stop.getRawLine2())) {
            snippet.append("<br/> RawLine2 : " + stop.getRawLine2());
        }
        if (!TextUtils.isEmpty(stop.getRawLine3())) {
            snippet.append("<br/> RawLine3 : " + stop.getRawLine3());
        }
        snippet.append("</body></html>");
        return snippet;
    }
}