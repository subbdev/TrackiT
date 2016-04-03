package com.subbu.trackit.restcontroller;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.subbu.trackit.CustomInfoWindowAdapter;
import com.subbu.trackit.beans.ResBean;
import com.subbu.trackit.beans.TruckStop;
import com.subbu.trackit.utils.Cache;
import com.subbu.trackit.utils.Util;

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

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public void getStopPoints(final LatLng latLng, double lat, double lng, final int radius,final Activity activity) {

        try {
            requestBody.put("lat",lat);
            requestBody.put("lng",lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final int curval = ++Util.currentCall;
        Log.i("@@@@@@@@", curval + "----" + Util.currentCall);
        if (Util.fromDB) {
            ArrayList<TruckStop> lst = Cache.getDatabaseAdapter().getTruckStopsByLocNRad(latLng, radius);
            map.clear();
            if (lst.size() > 0) {
                MarkerOptions marker = new MarkerOptions().position(latLng);
                map.addMarker(marker);
                if (Util.marker_icon == null)
                    Util.marker_icon = BitmapDescriptorFactory.fromBitmap(resizeMapIcons("truck_stop", 40, 60));
                for (TruckStop stop : lst) {
                    map.setInfoWindowAdapter(new CustomInfoWindowAdapter(activity, stop));
                    marker = new MarkerOptions()
                            .icon(Util.marker_icon)
                            .position(new LatLng(stop.getLat(), stop.getLng()));
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
                            Log.i("start #######", curval + "----" + Util.currentCall);
                            if (curval == Util.currentCall || radius == 50000) {
                                ResBean res = gson.fromJson(response.toString(), ResBean.class);
                                Log.i("&&&&&&&&&&&&&&&&", radius + "--" + res.getTruckStops().size() + "---" + map.getCameraPosition().zoom);
                                map.clear();
                                if (res.getTruckStops().size() > 0) {
                                    MarkerOptions marker = new MarkerOptions().position(latLng);
                                    map.addMarker(marker);
                                    if (Util.marker_icon == null)
                                        Util.marker_icon = BitmapDescriptorFactory.fromBitmap(resizeMapIcons("truck_stop", 40, 60));
                                    for (TruckStop stop : res.getTruckStops()) {
                                        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(activity, stop));
                                        marker = new MarkerOptions()
                                                .icon(Util.marker_icon)
                                                .position(new LatLng(stop.getLat(), stop.getLng()));
                                        map.addMarker(marker);
                                        Cache.getDatabaseAdapter().insertTruckStops(stop);
                                    }
                                }
                            }
                            Log.i("end #######", curval + "----" + Util.currentCall);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("ERROR", new String(error.networkResponse.data));
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

    public void setMap(GoogleMap map) {
        this.map = map;
    }
}