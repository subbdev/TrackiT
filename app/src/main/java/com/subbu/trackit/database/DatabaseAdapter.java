package com.subbu.trackit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.subbu.trackit.beans.TruckStop;

import java.util.ArrayList;


public class DatabaseAdapter {

    private static final String TAG = "DatabaseAdapter";

    /**
     * TruckStop table columns.
     */
    public static final String DATABASE_TABLE_TRUCK_STOP = "TruckStop";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_CITY = "City";
    public static final String COLUMN_STATE = "State";
    public static final String COLUMN_COUNTRY = "Country";
    public static final String COLUMN_ZIP = "Zip";
    public static final String COLUMN_LATITUDE = "Latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_RAW_LINE1 = "RawLine1";
    public static final String COLUMN_RAW_LINE2 = "RawLine2";
    public static final String COLUMN_RAW_LINE3 = "RawLine3";

    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context) {
        this.context = context;
    }

    public DatabaseAdapter open() throws SQLException {

        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }


    public void close() {
        dbHelper.close();
    }

    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen())
            open();
        return database;
    }

    /**
     * Inserts data into Truck stop.
     */
    public void insertTruckStops(TruckStop truckStop) {

        ContentValues values = new ContentValues();
        values.put(DatabaseAdapter.COLUMN_NAME, truckStop.getName());
        values.put(DatabaseAdapter.COLUMN_CITY, truckStop.getCity());
        values.put(DatabaseAdapter.COLUMN_STATE, truckStop.getState());
        values.put(DatabaseAdapter.COLUMN_COUNTRY, truckStop.getCountry());
        values.put(DatabaseAdapter.COLUMN_ZIP, truckStop.getZip());
        values.put(DatabaseAdapter.COLUMN_LATITUDE, truckStop.getLat());
        values.put(DatabaseAdapter.COLUMN_LONGITUDE, truckStop.getLng());
        values.put(DatabaseAdapter.COLUMN_RAW_LINE1, truckStop.getRawLine1());
        values.put(DatabaseAdapter.COLUMN_RAW_LINE2, truckStop.getRawLine2());
        values.put(DatabaseAdapter.COLUMN_RAW_LINE3, truckStop.getRawLine3());
        getDatabase().insert(DatabaseAdapter.DATABASE_TABLE_TRUCK_STOP, null, values);

    }

    /**
     * Deletes all Truck stop data in table
     */
    public void deleteTruckStops() {
        getDatabase().delete(DatabaseAdapter.DATABASE_TABLE_TRUCK_STOP, null, null);
    }

    /**
     * Gets all data from Truck stop table.
     */
    public ArrayList<TruckStop> getTruckStops() {

        ArrayList<TruckStop> truckStops = new ArrayList<>();
        String selection = null;
        String selectionArgs[] = null;
        String projection[] = new String[]{COLUMN_NAME,
                COLUMN_CITY,
                COLUMN_STATE,
                COLUMN_COUNTRY,
                COLUMN_ZIP,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_RAW_LINE1,
                COLUMN_RAW_LINE2,
                COLUMN_RAW_LINE3};

        Cursor cursor = getDatabase().query(DATABASE_TABLE_TRUCK_STOP, projection, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    TruckStop truckStop = new TruckStop();

                    truckStop.setName(cursor.getString(0));
                    truckStop.setCity(cursor.getString(1));
                    truckStop.setState(cursor.getString(2));
                    truckStop.setCountry(cursor.getString(3));
                    truckStop.setZip(cursor.getString(4));
                    truckStop.setLat(cursor.getDouble(5));
                    truckStop.setLng(cursor.getDouble(6));
                    truckStop.setRawLine1(cursor.getString(7));
                    truckStop.setRawLine2(cursor.getString(8));
                    truckStop.setRawLine3(cursor.getString(9));

                    truckStops.add(truckStop);
                }
            }
            cursor.close();
        }
        return truckStops;


    }

    /**
     * Gets all data from Truck stop table.
     */
    public ArrayList<TruckStop> getTruckStopsByLocNRad(LatLng center, float radius) {

        ArrayList<TruckStop> truckStops = new ArrayList<>();
        String selection = null;
        String selectionArgs[] = null;
        String projection[] = new String[]{COLUMN_NAME,
                COLUMN_CITY,
                COLUMN_STATE,
                COLUMN_COUNTRY,
                COLUMN_ZIP,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_RAW_LINE1,
                COLUMN_RAW_LINE2,
                COLUMN_RAW_LINE3};

        Cursor cursor = getDatabase().query(DATABASE_TABLE_TRUCK_STOP, projection, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    TruckStop truckStop = new TruckStop();
                    truckStop.setName(cursor.getString(0));
                    truckStop.setCity(cursor.getString(1));
                    truckStop.setState(cursor.getString(2));
                    truckStop.setCountry(cursor.getString(3));
                    truckStop.setZip(cursor.getString(4));
                    truckStop.setLat(cursor.getDouble(5));
                    truckStop.setLng(cursor.getDouble(6));
                    truckStop.setRawLine1(cursor.getString(7));
                    truckStop.setRawLine2(cursor.getString(8));
                    truckStop.setRawLine3(cursor.getString(9));
                    float[] test = new float[3];
                    Location.distanceBetween(center.latitude, center.longitude, truckStop.getLat(), truckStop.getLng(), test);
                    Log.i("RADIUS---", test[0] * 0.00062137 + "---" + radius);
                    if (test[0] * 0.00062137 <= radius)
                        truckStops.add(truckStop);
                }
            }
            cursor.close();
        }
        return truckStops;

    }

    public boolean isTruckStopsEmpty(){
        long count = DatabaseUtils.queryNumEntries(getDatabase(), DATABASE_TABLE_TRUCK_STOP);
        return count == 0 ? true : false;
    }

    /**
     * Gets list of truck stops based on name, city, state, and/or zip.
     * @return
     */
    public  ArrayList<TruckStop> getSearchTruckStops(String name,String city,String state,String zip){

        ArrayList<TruckStop> truckStops = new ArrayList<>();
        String selection = "lower(" + COLUMN_NAME + ") like ? ";
        ArrayList<String> selectionArgs = new ArrayList<>();
        int i = 0;

        String searchString = "%" + name.toLowerCase() + "%";
        selectionArgs.add(searchString);

        if(city.length() > 0){
            selection += "AND lower(" + COLUMN_CITY + ") =?";
            selectionArgs.add(city.toLowerCase()) ;
        }

        if(state.length() > 0){
            selection += "AND lower(" + COLUMN_STATE + ") =?";
            selectionArgs.add(state.toLowerCase());
        }

        if(zip.length() > 0){
            selection += "AND lower(" + COLUMN_ZIP + ") =?";
            selectionArgs.add(zip.toLowerCase());
        }

        String projection[] = new String[]{COLUMN_NAME,
                COLUMN_CITY,
                COLUMN_STATE,
                COLUMN_COUNTRY,
                COLUMN_ZIP,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_RAW_LINE1,
                COLUMN_RAW_LINE2,
                COLUMN_RAW_LINE3};

        Cursor cursor = getDatabase().query(DATABASE_TABLE_TRUCK_STOP, projection, selection, selectionArgs.toArray(new String[selectionArgs.size()]), null, null, null);


        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    TruckStop truckStop = new TruckStop();
                    truckStop.setName(cursor.getString(0));
                    truckStop.setCity(cursor.getString(1));
                    truckStop.setState(cursor.getString(2));
                    truckStop.setCountry(cursor.getString(3));
                    truckStop.setZip(cursor.getString(4));
                    truckStop.setLat(cursor.getDouble(5));
                    truckStop.setLng(cursor.getDouble(6));
                    truckStop.setRawLine1(cursor.getString(7));
                    truckStop.setRawLine2(cursor.getString(8));
                    truckStop.setRawLine3(cursor.getString(9));
                    truckStops.add(truckStop);
                }
            }
            cursor.close();
        }
        return truckStops;

    }
}



