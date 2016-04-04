package com.ggk.transflo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ggk.transflo.R;


public class ConnectivityUtil {
    public static void check(final Activity activity) {
        if (isConnectingToInternet(activity)) {
            checkGPSStatus(activity);
        } else {
            final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

            // Setting Dialog Title
            alertDialog.setTitle("Track me");

            // Setting Dialog Message
            alertDialog.setMessage(activity.getString(R.string.network_turn_on));
            alertDialog.setCanceledOnTouchOutside(false);

            // Setting OK Button
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                    checkGPSStatus(activity);
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }
    }

    public static void checkGPSStatus(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!(isGPSEnabled && isNetworkEnabled)) {
            Util.showSettingsAlert(activity);
        }
    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }
}
