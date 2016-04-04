package com.ggk.trackit;

import android.app.Activity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;


    public CustomInfoWindowAdapter(Activity context) {
        mWindow = context.getLayoutInflater().inflate(R.layout.custom_info_window, null);
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
        TextView addressTextView = (TextView) view.findViewById(R.id.textview_address);

        nameTextView.setText(marker.getTitle());
        addressTextView.setText(TextUtils.isEmpty(marker.getSnippet()) ? "" : Html.fromHtml(marker.getSnippet()));

    }
}