package com.subbu.trackit.restcontroller;

import android.app.Application;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.subbu.trackit.beans.ResBean;
import com.subbu.trackit.beans.TruckStop;

import org.json.JSONException;
import org.json.JSONObject;

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


    public void getStopPoints(final LatLng latLng, double lat, double lng, final int radius) {

        try {
            requestBody.put("lat",lat);
            requestBody.put("lng",lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                BASE_URL+radius, requestBody,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ResBean res = gson.fromJson(response.toString(), ResBean.class);
                        Log.i("&&&&&&&&&&&&&&&&", radius + "--" + res.getTruckStops().size() + "---" + map.getCameraPosition().zoom);
                        if(res.getTruckStops().size()>0) {
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            map.clear();
                            MarkerOptions marker = new MarkerOptions().position(latLng);
                            map.addMarker(marker);
                            for (TruckStop stop : res.getTruckStops()) {
                                marker = new MarkerOptions().position(new LatLng(stop.getLat(), stop.getLng()));
                                map.addMarker(marker);
                            }


                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", new String(error.networkResponse.data));
            }
        }) {
            /**
             * Passing some request headers
             * */
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

    public void setMap(GoogleMap map) {
        this.map = map;
    }
}