package com.subbu.trackit;

import android.app.Activity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.subbu.trackit.beans.TruckStop;

/**
 * Created by bhanuchander.belladi on 01-04-2016.
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private TruckStop mTruckStop;


    public CustomInfoWindowAdapter(Activity context, TruckStop stop) {
        mWindow = context.getLayoutInflater().inflate(R.layout.custom_info_window, null);
        mTruckStop = stop;
    }

    @Override
    public View getInfoWindow(Marker marker) {


        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {

        TextView nameTextView = (TextView) view.findViewById(R.id.textview_name);
//        TextView distanceTextView = (TextView) view.findViewById(R.id.textview_distance);
        TextView addressTextView = (TextView) view.findViewById(R.id.textview_address);

        nameTextView.setText(marker.getTitle());
        addressTextView.setText(TextUtils.isEmpty(marker.getSnippet()) ? "" : Html.fromHtml(marker.getSnippet()));
//        addressTextView.setText(stop.getCity()+", "+stop.getState()+", "+stop.getCountry());

    }
}