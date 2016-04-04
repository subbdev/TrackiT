package com.subbu.trackit.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.subbu.trackit.R;

/**
 * Created by Subrahmanyam.yepuri on 01-04-2016.
 */
public class Util {
    public static int currentCall = 0;
    public static BitmapDescriptor marker_icon;
    public static boolean fromDB = false;
    public static boolean isManualMove = false;
    public static boolean isTracking = false;

    public static long radiusToZoom(float radius) {
        double scale = radius * 1609.34 / 540;
        return Math.round(16 - Math.log(scale) / Math.log(2));
    }
    public static int getCurrentRadius(GoogleMap mMap, float width,float dpi)
    {
       /* LatLngBounds llBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        float[] test = new float[3];
        Location.distanceBetween(llBounds.northeast.latitude, llBounds.northeast.longitude, llBounds.southwest.latitude, llBounds.southwest.longitude, test);
        Log.i("DISTinti********", test[0] + "");
        int radius = (int) (((test[0] / Math.sqrt(1 + dratio)) * 0.00062137) / 2);*/
        double radius = (40075160 * 160.0 * width) * 0.000621371 / (Math.pow(2, mMap.getCameraPosition().zoom + 1) * dpi * 256);

        return (int) radius;
    }
    public static Bitmap resizeMapIcons(Activity activity, int iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(activity.getResources(), iconName);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public static void showSettingsAlert(final Activity activity){
        String title = activity.getResources().getString(R.string.track_me);

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }


}
